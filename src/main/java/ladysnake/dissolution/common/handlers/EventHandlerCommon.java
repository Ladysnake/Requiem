package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler.Provider;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;


/**
 * This class handles basic events-related logic
 * It is mostly used to cancel player interactions when the latter is a ghost
 * @author Pyrofab
 *
 */
public class EventHandlerCommon {

	/**
	 * Attaches a {@link IncorporealDataHandler} to players.
	 * @param event
	 */
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {

		if (!(event.getObject() instanceof EntityPlayer))
			return;

		event.addCapability(new ResourceLocation(Reference.MOD_ID, "incorporeal"), new Provider());
	}

	@SubscribeEvent
	public void clonePlayer(PlayerEvent.Clone event) {
		if (event.isWasDeath() && !event.getEntityPlayer().isCreative()) {
			event.getEntityPlayer().experienceLevel = event.getOriginal().experienceLevel;
			final IIncorporealHandler corpse = IncorporealDataHandler.getHandler(event.getOriginal());
			final IIncorporealHandler clone = IncorporealDataHandler.getHandler(event.getEntityPlayer());
			clone.setIncorporeal(true, event.getEntityPlayer());
			clone.setLastDeathMessage(corpse.getLastDeathMessage());
			clone.setSynced(false);
			/*IMessage msg = new IncorporealMessage(event.getEntityPlayer().getUniqueID().getMostSignificantBits(),
					event.getEntityPlayer().getUniqueID().getLeastSignificantBits(), true);
			PacketHandler.net.sendToAll(msg);*/
			
			if(DissolutionConfig.respawnInNether && !DissolutionConfig.wowRespawn)
				event.getEntityPlayer().setPosition(event.getOriginal().posX, event.getOriginal().posY, event.getOriginal().posZ);
		}
	}

	/**
	 * Makes the player practically invisible to mobs
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onVisibilityPlayer(PlayerEvent.Visibility event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.isIncorporeal())
			event.modifyVisibility(0D);
	}

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

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerAttackEntity(AttackEntityEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative()) {
			if (event.isCancelable())
				event.setCanceled(true);
			return;
		}
		if (event.getTarget() instanceof EntityPlayer) {
			final IIncorporealHandler targetCorp = IncorporealDataHandler.getHandler(event.getTarget());
			if (playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative())
				if (event.isCancelable())
					event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityItemPickup(EntityItemPickupEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative()) {
			if (event.isCancelable())
				event.setCanceled(true);
		}
	}

	/**
	 * Makes the players tangible again when stroke by lightning. Just because we can.
	 * @param event
	 */
	@SubscribeEvent
	public void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
		if (event.getEntity() instanceof EntityPlayer) {
			final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler((EntityPlayer) event.getEntity());
			if (playerCorp.isIncorporeal()) {
				playerCorp.setIncorporeal(false, (EntityPlayer) event.getEntity());
				/*IMessage msg = new IncorporealMessage(event.getEntity().getUniqueID().getMostSignificantBits(),
						event.getEntity().getUniqueID().getLeastSignificantBits(), playerCorp.isIncorporeal());
				PacketHandler.net.sendToAll(msg);*/
			}
		}
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
