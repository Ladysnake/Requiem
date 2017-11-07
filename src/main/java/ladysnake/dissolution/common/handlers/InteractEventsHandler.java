package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.IPossessable;
import ladysnake.dissolution.api.ISoulInteractable;
import ladysnake.dissolution.common.DissolutionConfigManager;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.entity.souls.AbstractSoul;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PossessionMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class InteractEventsHandler {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		IIncorporealHandler status = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
		if (!event.getEntityPlayer().isCreative()
				&& status.getPossessed() == null
				&& (status.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL
				|| (status.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.ECTOPLASM
				&& !DissolutionConfigManager.canEctoplasmInteractWith(event.getWorld().getBlockState(event.getPos()).getBlock())))) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if(isGhost(event)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onGetCollisionBoxes(GetCollisionBoxesEvent event) {
		if(event.getEntity() instanceof AbstractSoul
				|| event.getEntity() instanceof EntityPlayer
				&& CapabilityIncorporealHandler.getHandler((EntityPlayer)event.getEntity())
						.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL)
		event.getCollisionBoxesList().removeIf(aaBB -> aaBB.getAverageEdgeLength() < 1);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if(isGhost(event)
				&& !DissolutionConfigManager.canEctoplasmInteractWith(event.getItemStack().getItem()))
			event.setCanceled(true);
	}

	/**
	 * Allows possession start
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		event.setCanceled(isGhost(event));
		if (isGhost(event) && event.getSide().isServer() && event.getTarget() instanceof EntityLivingBase && !event.getTarget().getIsInvulnerable()) {
			IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
			IPossessable host = AbstractMinion.createMinion((EntityLivingBase) event.getTarget());
			if (host instanceof EntityLivingBase && host.canBePossessedBy(event.getEntityPlayer())) {
				EntityLivingBase eHost = (EntityLivingBase)host;
				if(((EntityLivingBase)event.getTarget()).getHeldItemMainhand().getItem() instanceof ItemBow)
					event.getEntityPlayer().addItemStackToInventory(new ItemStack(Items.ARROW, eHost.world.rand.nextInt(10)+2));
				DissolutionInventoryHelper.transferEquipment((EntityLivingBase) event.getTarget(), event.getEntityPlayer());
				if(host != event.getTarget()) {
					event.getTarget().setPosition(0,-100,0);
					event.getTarget().world.spawnEntity(eHost);
					event.getTarget().world.removeEntity(event.getTarget());
				}
				handler.setPossessed(host);
				event.setCancellationResult(EnumActionResult.SUCCESS);
			}
		}
	}

	@SubscribeEvent
	public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
		if (handler.getCorporealityStatus().isIncorporeal() && handler.getPossessed() != null)
			event.setNewSpeed(event.getOriginalSpeed()*5);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if(isGhost(event) && !(event.getTarget() instanceof ISoulInteractable)) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void onItemUseStart(LivingEntityUseItemEvent.Start event) {
		final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
		if(handler != null && handler.getPossessed() instanceof EntityLivingBase) {		// synchronizes item use between player and possessed entity
			((EntityLivingBase)handler.getPossessed()).setActiveHand(event.getEntityLiving().getHeldItemMainhand().equals(event.getItem()) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
		}
	}
	
	@SubscribeEvent
	public void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
		if(event.getDuration() <= 1) {			// prevent the player from finishing the item use if possessing an entity
			final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
			if(handler != null && handler.getPossessed() instanceof EntityLivingBase) {
				event.getEntityLiving().resetActiveHand();
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void onItemUseStop(LivingEntityUseItemEvent.Stop event) {
		final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
		if(handler != null && handler.getPossessed() instanceof EntityLivingBase) {
			((EntityLivingBase)handler.getPossessed()).stopActiveHand();
			event.setCanceled(true);			// prevent the player from duplicating the action
		}
	}

	/**
	 * Prevents a player from ending possession prematurely
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityMount(EntityMountEvent event) {
		IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
		if (!event.isMounting() && handler != null && handler.getCorporealityStatus().isIncorporeal()
				&& !(event.getEntity() instanceof EntityPlayer && ((EntityPlayer)event.getEntity()).isCreative())) {
			if(!event.isMounting()) {
				if (event.getEntityBeingMounted() == handler.getPossessed() && !(handler.setPossessed(null))) {
					if(event.getEntity().isSneaking() && event.getEntity() instanceof EntityPlayer)
						PlayerTickHandler.sneakingPossessingPlayers.add((EntityPlayer) event.getEntity());
					event.setCanceled(true);
				}
			}
		}
	}

	/**
	 * Checks if the player from the event is intangible
	 * @return true if the event's entity is a non-creative player and a ghost
	 */
	private static boolean isGhost(PlayerEvent event) {
		IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
		return handler.getCorporealityStatus().isIncorporeal() && handler.getPossessed() == null
				&& !event.getEntityPlayer().isCreative();
	}

}
