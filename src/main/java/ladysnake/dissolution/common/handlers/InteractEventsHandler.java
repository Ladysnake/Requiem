package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.entity.souls.AbstractSoul;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;

public class InteractEventsHandler {

    @SubscribeEvent
    public void onGetCollisionBoxes(GetCollisionBoxesEvent event) {
        if (event.getEntity() instanceof AbstractSoul
                || event.getEntity() instanceof EntityPlayer
                && CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity())
                .getCorporealityStatus() == SoulStates.SOUL) {
            final Iterator<AxisAlignedBB> iterator = event.getCollisionBoxesList().iterator();
            while (iterator.hasNext())
                if (iterator.next().getAverageEdgeLength() < Dissolution.config.ghost.maxThickness)
                    iterator.remove();
        }
    }

    /**
     * Allows possession start
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (!(handler.getCorporealityStatus().isIncorporeal() && handler.getPossessed() == null
                && !event.getEntityPlayer().isCreative())) return;
        event.setCanceled(true);
        if (handler.getCorporealityStatus().isIncorporeal() && event.getSide().isServer()
                && event.getTarget() instanceof EntityLivingBase && !event.getTarget().getIsInvulnerable()) {
            IPossessable host = AbstractMinion.createMinion((EntityLivingBase) event.getTarget());
            if (host instanceof EntityLivingBase && host.canBePossessedBy(event.getEntityPlayer())) {
                EntityLivingBase eHost = (EntityLivingBase) host;
                if (((EntityLivingBase) event.getTarget()).getHeldItemMainhand().getItem() instanceof ItemBow)
                    event.getEntityPlayer().addItemStackToInventory(new ItemStack(Items.ARROW, eHost.world.rand.nextInt(10) + 2));
                DissolutionInventoryHelper.transferEquipment((EntityLivingBase) event.getTarget(), event.getEntityPlayer());
                if (host != event.getTarget()) {
                    event.getTarget().setPosition(0, -100, 0);
                    event.getTarget().world.spawnEntity(eHost);
                    event.getTarget().world.removeEntity(event.getTarget());
                }
                handler.setPossessed(host);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
        if (handler != null && handler.getPossessed() instanceof EntityLivingBase) {        // synchronizes item use between player and possessed entity
            ((EntityLivingBase) handler.getPossessed()).setActiveHand(event.getEntityLiving().getHeldItemMainhand().equals(event.getItem()) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
        }
    }

    @SubscribeEvent
    public void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getDuration() <= 1) {            // prevent the player from finishing the item use if possessing an entity
            final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
            if (handler != null && handler.getPossessed() instanceof EntityLivingBase) {
                event.getEntityLiving().resetActiveHand();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onItemUseStop(LivingEntityUseItemEvent.Stop event) {
        final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
        if (handler != null && handler.getPossessed() instanceof EntityLivingBase) {
            ((EntityLivingBase) handler.getPossessed()).stopActiveHand();
            event.setCanceled(true);            // prevent the player from duplicating the action
        }
    }

    /**
     * Prevents a player from ending possession prematurely
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityMount(EntityMountEvent event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
        if (!event.isMounting() && handler != null && handler.getCorporealityStatus().isIncorporeal()) {
            if ((event.getEntity() instanceof EntityPlayer && ((EntityPlayer) event.getEntity()).isCreative()))
                handler.setPossessed(null);
            else {
                if (event.getEntityBeingMounted() == handler.getPossessed() && !(handler.setPossessed(null))) {
                    if (event.getEntity().isSneaking() && event.getEntity() instanceof EntityPlayer)
                        PlayerTickHandler.sneakingPossessingPlayers.add((EntityPlayer) event.getEntity());
                    event.setCanceled(true);
                }
            }
        }
    }
}
