package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.PossessableEntityFactory;
import ladysnake.dissolution.common.entity.souls.AbstractSoul;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InteractEventsHandler {

    /**
     * Make wisps and soul players go through thin walls
     */
    @SubscribeEvent
    public void onGetCollisionBoxes(GetCollisionBoxesEvent event) {
        if ((event.getEntity() instanceof AbstractSoul)
                || ((event.getEntity() instanceof EntityPlayer)
                && (CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity())
                .getCorporealityStatus() == SoulStates.SOUL))) {
            event.getCollisionBoxesList().removeIf(axisAlignedBB -> axisAlignedBB.getAverageEdgeLength() < Dissolution.config.ghost.maxThickness);
        }
    }

    /**
     * Allows possession start
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (!(handler.getCorporealityStatus().isIncorporeal() && handler.getPossessed() == null
                && !event.getEntityPlayer().isCreative())) {
            return;
        }
        event.setCanceled(true);
        if (handler.getCorporealityStatus().isIncorporeal()) {
            if (event.getTarget() instanceof ISoulInteractable) {
                event.setCancellationResult(((ISoulInteractable) event.getTarget()).applySoulInteraction(event.getEntityPlayer(), event.getLocalPos(), event.getHand()));
            } else if (event.getSide().isServer()
                    && event.getTarget() instanceof EntityLivingBase && !event.getTarget().getIsInvulnerable()) {
                EntityLivingBase host = PossessableEntityFactory.createMinion((EntityLivingBase) event.getTarget());
                if (host != null && ((IPossessable)host).canBePossessedBy(event.getEntityPlayer())) {
                    if (((EntityLivingBase) event.getTarget()).getHeldItemMainhand().getItem() instanceof ItemBow) {
                        event.getEntityPlayer().addItemStackToInventory(new ItemStack(Items.ARROW, host.world.rand.nextInt(10) + 2));
                    }
                    DissolutionInventoryHelper.transferEquipment((EntityLivingBase) event.getTarget(), event.getEntityPlayer());
                    if (host != event.getTarget()) {
                        event.getTarget().setPosition(0, -100, 0);
                        event.getTarget().world.spawnEntity(host);
                        event.getTarget().world.removeEntity(event.getTarget());
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
