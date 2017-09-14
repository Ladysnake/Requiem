package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.ISoulInteractable;
import ladysnake.dissolution.api.SoulTypes;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InteractEventsHandler {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (isGhost(event) && !(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof ISoulInteractable)) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if(isGhost(event)) {
			event.setCanceled(true);
			
			if(!DissolutionConfig.wip.enableSoulDash) return;
			
			final IIncorporealHandler ghostHandler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
			ghostHandler.setIntangible(true);
			
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if(isGhost(event) && !(event.getItemStack().getItem() instanceof ISoulInteractable))
			event.setCanceled(true);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		EntityPlayer player = event.getEntityPlayer();
		if(isGhost(event) && !(event.getTarget() instanceof ISoulInteractable)) {
			if(event.getTarget() instanceof EntityLiving && 
					SoulTypes.getSoulFor((EntityLiving) event.getTarget()) == SoulTypes.BENIGN && 
					event.getTarget() != event.getEntityPlayer().getRidingEntity()) {
				player.startRiding(event.getTarget(), true);
				player.eyeHeight = event.getTarget().getEyeHeight();
				player.setInvisible(true);
				if(!player.world.isRemote) {
					fakeSpectator((EntityPlayerMP)player, event.getTarget());
				}
			}
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if(isGhost(event) && !(event.getTarget() instanceof ISoulInteractable))
			event.setCanceled(true);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityMount(EntityMountEvent event) {
		if(event.isMounting() && isGhost(event)) {
			
		} else if(isGhost(event)) {
			final EntityPlayer player = (EntityPlayer)event.getEntity();
			((EntityPlayer)event.getEntity()).eyeHeight = ((EntityPlayer)event.getEntity()).getDefaultEyeHeight();
			((EntityPlayer)event.getEntity()).setInvisible(DissolutionConfig.ghost.invisibleGhosts);
			if(!player.world.isRemote) {
				fakeSpectator((EntityPlayerMP)player, player);
			}
			if(event.getEntityBeingMounted() instanceof EntityLiving)
				((EntityLiving)event.getEntityBeingMounted()).setNoAI(false);
		}
	}
	
	private void fakeSpectator(EntityPlayerMP player, Entity entityToSpectate) {
        Entity spectatingEntity = (Entity)(entityToSpectate == null ? this : entityToSpectate);

		//((EntityPlayerMP)player).setSpectatingEntity(entityToSpectate);
        //player.connection.sendPacket(new SPacketCamera(spectatingEntity));
	}
	
	/**
	 * Checks if the player from the event is intangible
	 * @param event
	 * @return true if the event's entity is a non-creative player and a ghost
	 */
	private static boolean isGhost(EntityEvent event) {
		return event.getEntity() instanceof EntityPlayer && 
				CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity()).isIncorporeal() && 
				!((EntityPlayer)event.getEntity()).isCreative();
	}
	
	/**
	 * Same as {@link #isGhost(EntityEvent)} except optimized for player events.
	 */
	private static boolean isGhost(PlayerEvent event) {
		return CapabilityIncorporealHandler.getHandler(event.getEntityPlayer()).isIncorporeal() && !event.getEntityPlayer().isCreative();
	}

}
