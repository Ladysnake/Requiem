package ladysnake.dissolution.common.handlers;

import ladylib.compat.EnhancedBusSubscriber;
import ladylib.misc.ReflectionFailedException;
import ladylib.reflection.TypedReflection;
import ladylib.reflection.typed.TypedMethod0;
import ladylib.reflection.typed.TypedMethod1;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.util.DelayedTaskRunner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Optional;

import static ladysnake.dissolution.common.Ref.MOD_ID;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@EnhancedBusSubscriber(MOD_ID)
public class PossessionEventHandler {
    private static final TypedMethod1<AbstractSkeleton, Float, EntityArrow> abstractSkeleton$getArrow =
            TypedReflection.findMethod(AbstractSkeleton.class, "func_190726_a", EntityArrow.class, float.class);

    @SubscribeEvent
    public void onTickPlayerTick(TickEvent.PlayerTickEvent event) {
        // Tick a bunch of possession
        if (event.phase != TickEvent.Phase.START) return;
        IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
        IPossessable possessed = playerCorp.getPossessed();
        if (possessed != null) {
            possessed.updatePossessing();
        }
    }

    @SubscribeEvent
    public void onSkeletonFires(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityArrow && !event.getWorld().isRemote) {
            EntityArrow arrow = (EntityArrow) event.getEntity();
            CapabilityIncorporealHandler.getHandler(arrow.shootingEntity).ifPresent(handler -> {
                EntityLivingBase possessed = handler.getPossessed();
                if (possessed instanceof AbstractSkeleton) {
                    try {
                        EntityArrow mobArrow = abstractSkeleton$getArrow.invoke((AbstractSkeleton) possessed, 0f);
                        mobArrow.setDamage(arrow.getDamage());
                        mobArrow.copyLocationAndAnglesFrom(arrow);
                        mobArrow.motionX = arrow.motionX;
                        mobArrow.motionY = arrow.motionY;
                        mobArrow.motionZ = arrow.motionZ;
                        arrow.world.spawnEntity(mobArrow);
                        event.setCanceled(true);
                    } catch (ReflectionFailedException e) {
                        Dissolution.LOGGER.warn("Failed to get an arrow from a skeleton", e);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        CapabilityIncorporealHandler.getHandler(event.getEntity()).map(IIncorporealHandler::getPossessed).ifPresent(p -> ((EntityLivingBase) p).fall(event.getDistance(), event.getDamageMultiplier()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        // Properly delegate attacks from/to players during possession
        Optional<EntityLivingBase> targetPossessed = CapabilityIncorporealHandler.getHandler(event.getEntity()).map(IIncorporealHandler::getPossessed);
        if (targetPossessed.isPresent()) {
            // Set the target to the possessed entity, not the soul controlling it
            targetPossessed.get().attackEntityFrom(event.getSource(), event.getAmount());
            event.setCanceled(true);
        } else {
            // Attack from the possessed entity, not the soul controlling it
            // This branch will be indirectly/recursively called from the first through `attackEntityFrom`, no need for fancy handling
            Optional<IPossessable> attackerPossessed = CapabilityIncorporealHandler.getHandler(event.getSource().getTrueSource()).map(IIncorporealHandler::getPossessed);
            if (attackerPossessed.isPresent()) {
                if (attackerPossessed.get().proxyAttack(event.getEntityLiving(), event.getSource(), event.getAmount())) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        // Removes the player's possessed entity so that it doesn't wander around
        EntityPlayer player = event.player;
        IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);
        IPossessable possessed = playerCorp.getPossessed();
        World world = player.world;
        if (possessed != null && !world.isRemote) {
            possessed.markForLogOut();
        }
    }

    @SubscribeEvent
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        /*
        * The dimension change is usually handled by the possessed entity itself,
        * but some teleporters may teleport the player first, or may refuse to teleport the entity.
        * In that case, we handle it as a logout, and pray for the best.
        */
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);
            IPossessable possessed = playerCorp.getPossessed();
            World world = player.world;
            if (possessed != null && !world.isRemote) {
                // Save the entity ourselves as data is not written to disk during a dimension change
                playerCorp.setSerializedPossessedEntity(((Entity)possessed).serializeNBT());
                possessed.markForLogOut();
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity player = event.getEntity();
        if (player.world.isRemote) {
            return;
        }
        CapabilityIncorporealHandler.getHandler(player).ifPresent(handler -> {
            final NBTTagCompound serializedPossessedEntity = handler.getSerializedPossessedEntity();
            if (serializedPossessedEntity != null) {
                DelayedTaskRunner.INSTANCE.addDelayedTask(player.dimension, 1, () -> {
                    Entity host = EntityList.createEntityFromNBT(serializedPossessedEntity, event.getWorld());
                    if (host instanceof EntityLivingBase && host instanceof IPossessable) {
                        host.setPosition(player.posX, player.posY, player.posZ);
                        event.getWorld().spawnEntity(host);
                        handler.setPossessed((EntityLivingBase & IPossessable) host);
                        // Sometimes the client doesn't get notified, so we update again half a second later to be sure
                        DelayedTaskRunner.INSTANCE.addDelayedTask(player.dimension, 10, () -> handler.setPossessed((EntityLivingBase & IPossessable) host));
                    } else {
                        Dissolution.LOGGER.warn("{}'s possessed entity could not be deserialized", player);
                    }
                });
            }
        });
    }

    private boolean messingWithPotions = false;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        Entity entity = event.getEntity();
        if (entity != null && entity.world != null && !entity.world.isRemote && !messingWithPotions) {
            messingWithPotions = true;
            if (entity instanceof IPossessable) {
                IPossessable possessable = (IPossessable) entity;
                EntityPlayer possessor = possessable.getPossessingEntity();
                if (possessor != null) {
                    possessor.addPotionEffect(new PotionEffect(event.getPotionEffect()));
                }
            } else {
                CapabilityIncorporealHandler.getHandler(entity)
                        .map(IIncorporealHandler::getPossessed)
                        .ifPresent(possessed -> ((EntityLivingBase) possessed).addPotionEffect(new PotionEffect(event.getPotionEffect())));
            }
            messingWithPotions = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPotionRemove(PotionEvent.PotionRemoveEvent event) {
        Entity entity = event.getEntity();
        if (entity != null && entity.world != null && !entity.world.isRemote && !messingWithPotions) {
            messingWithPotions = true;
            if (entity instanceof IPossessable) {
                IPossessable possessable = (IPossessable) entity;
                EntityPlayer possessor = possessable.getPossessingEntity();
                if (possessor != null) {
                    possessor.removePotionEffect(event.getPotion());
                }
            } else {
                CapabilityIncorporealHandler.getHandler(entity)
                        .map(IIncorporealHandler::getPossessed)
                        .ifPresent(possessed -> ((EntityLivingBase) possessed).removePotionEffect(event.getPotion()));
            }
            messingWithPotions = false;
        }
    }

    @EnhancedBusSubscriber(value = MOD_ID, side = CLIENT)
    public static class Client {

        public static final TypedMethod0<EntityPlayerSP, Void> ON_UPDATE_WALKING_PLAYER = TypedReflection.findMethod(EntityPlayerSP.class, "func_175161_p", void.class);

        @SubscribeEvent
        public void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
            EntityPlayerSP self = Minecraft.getMinecraft().player;
            if (event.player == self) {
                Entity possessed = CapabilityIncorporealHandler.getHandler(self).getPossessed();
                if (possessed != null) {
                    if (event.phase == TickEvent.Phase.START) {
                        Minecraft.getMinecraft().setRenderViewEntity(self);
                    } else {
                        ON_UPDATE_WALKING_PLAYER.invoke(self);
                        Minecraft.getMinecraft().setRenderViewEntity(possessed);
                    }
                }
            }
        }

    }
}
