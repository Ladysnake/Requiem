package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.PossessionEvent;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.PossessableEntityFactory;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.registries.SoulStates;
import ladysnake.dissolution.unused.common.entity.souls.AbstractSoul;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InteractEventsHandler {

    public static final double MAX_THICCNESS = 0.9;     // you are welcome

    /**
     * Make wisps and soul players go through thin walls
     */
    @SubscribeEvent
    public void onGetCollisionBoxes(GetCollisionBoxesEvent event) {
        if ((event.getEntity() instanceof AbstractSoul)
                || ((event.getEntity() instanceof EntityPlayer)
                && (CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity())
                .getCorporealityStatus() == SoulStates.SOUL))) {
            event.getCollisionBoxesList().removeIf(axisAlignedBB -> axisAlignedBB.getAverageEdgeLength() < MAX_THICCNESS);
        }
    }

    /**
     * Allows possession start
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        EntityPlayer player = event.getEntityPlayer();
        Entity target = event.getTarget();
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
        if (!(handler.getCorporealityStatus().isIncorporeal() && handler.getPossessed() == null
                && !player.isCreative())) {
            return;
        }
        event.setCanceled(true);
        if (handler.getCorporealityStatus().isIncorporeal()) {
            if (target instanceof ISoulInteractable) {
                event.setCancellationResult(((ISoulInteractable) target).applySoulInteraction(player, event.getLocalPos(), event.getHand()));
            } else if (event.getSide().isServer()
                    && target instanceof EntityLivingBase && !target.getIsInvulnerable()) {
                PossessionEvent.Setup setupEvent = new PossessionEvent.Setup(
                        player,
                        (EntityLivingBase) target,
                        PossessableEntityFactory.createMinion((EntityLivingBase) target)
                );
                if (MinecraftForge.EVENT_BUS.post(setupEvent)) {
                    return;
                }
                EntityLivingBase host = setupEvent.getPossessed();
                if (host != null && ((IPossessable)host).canBePossessedBy(player)) {
                    if (((EntityLivingBase) target).getHeldItemMainhand().getItem() instanceof ItemBow) {
                        player.addItemStackToInventory(new ItemStack(Items.ARROW, host.world.rand.nextInt(10) + 2));
                    }
                    DissolutionInventoryHelper.transferEquipment((EntityLivingBase) target, player);
                    if (host != target) {
                        target.setPosition(0, -100, 0);
                        target.world.spawnEntity(host);
                        target.world.removeEntity(target);
                    }
                    handler.setPossessed((EntityMob & IPossessable) host);
                    event.setCancellationResult(EnumActionResult.SUCCESS);
                }
            }
        }
    }

    /**
     * Prevent possessed mobs from shooting themselves
     */
    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent.Arrow event) {
        if (event.getRayTraceResult().typeOfHit == RayTraceResult.Type.ENTITY &&
                        CapabilityIncorporealHandler.getHandler(event.getArrow().shootingEntity)
                                .map(IIncorporealHandler::getPossessed)
                                .map(event.getRayTraceResult().entityHit::equals)
                                .orElse(false)) {
            event.setCanceled(true);
        }
    }



    /**
     * Prevents a player from ending possession prematurely
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityMount(EntityMountEvent event) {
        if (event.getEntityMounting().world.isRemote) {
            return;
        }
        CapabilityIncorporealHandler.getHandler(event.getEntity()).ifPresent(handler -> {
            if (handler.getCorporealityStatus().isIncorporeal()) {
                Entity beingMounted = event.getEntityBeingMounted();
                if (event.isMounting()) {
                    if (!beingMounted.getUniqueID().equals(handler.getPossessedUUID()) && handler.getPossessed() != null) {
                        handler.getPossessed().startRiding(beingMounted);
                        event.setCanceled(true);
                    }
                } else {
                    EntityLivingBase possessed = handler.getPossessed();
                    if (possessed != null && event.getEntity() instanceof EntityPlayer && ((EntityPlayer) event.getEntity()).isCreative()) {
                        handler.setPossessed(null);
                    } else {
                        if (beingMounted == possessed && !(handler.setPossessed(null))) {
                            if (possessed != null && possessed.isRiding()) {
                                possessed.dismountRidingEntity();
                            } else if (event.getEntity().isSneaking() && event.getEntity() instanceof EntityPlayer) {
                                PlayerTickHandler.sneakingPossessingPlayers.add((EntityPlayer) event.getEntity());
                            }
                            event.setCanceled(true);
                        }
                    }
                }
            }
        });
    }
}
