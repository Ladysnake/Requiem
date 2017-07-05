package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InteractEventsHandler {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (intangible(event) && !(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof ISoulInteractable)) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if(intangible(event))
			event.setCanceled(true);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if(intangible(event) && !(event.getItemStack().getItem() instanceof ISoulInteractable))
			event.setCanceled(true);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		if(intangible(event) && !(event.getTarget() instanceof ISoulInteractable))
			event.setCanceled(true);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if(intangible(event) && !(event.getTarget() instanceof ISoulInteractable))
			event.setCanceled(true);
	}
	
	/**
	 * Checks if the player from the event is intangible
	 * @param event
	 * @return true if the event's entity is a non-creative player and a ghost
	 */
	private boolean intangible(EntityEvent event) {
		return event.getEntity() instanceof EntityPlayer && 
				IncorporealDataHandler.getHandler((EntityPlayer) event.getEntity()).isIncorporeal() && 
				!((EntityPlayer)event.getEntity()).isCreative();
	}
	
	/**
	 * Same as {@link #intangible(EntityEvent)} except optimized for player events.
	 */
	private boolean intangible(PlayerEvent event) {
		return IncorporealDataHandler.getHandler(event.getEntityPlayer()).isIncorporeal() && !event.getEntityPlayer().isCreative();
	}

}
