package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


/**
 * This class handles basic events-related logic
 * It is mostly used to cancel player interactions when the latter is a ghost
 * @author Pyrofab
 *
 */
public class EventHandlerCommon {

	/**
	 * Attaches a {@link CapabilityIncorporealHandler} to players.
	 * @param event
	 */
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {

		if (!(event.getObject() instanceof EntityPlayer))
			return;

		event.addCapability(new ResourceLocation(Reference.MOD_ID, "incorporeal"), new CapabilityIncorporealHandler.Provider((EntityPlayer) event.getObject()));
	}

	@SubscribeEvent
	public void clonePlayer(PlayerEvent.Clone event) {
		if (event.isWasDeath() && !event.getEntityPlayer().isCreative()) {
			event.getEntityPlayer().experienceLevel = event.getOriginal().experienceLevel;
			final IIncorporealHandler corpse = CapabilityIncorporealHandler.getHandler(event.getOriginal());
			final IIncorporealHandler clone = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
			clone.setIncorporeal(true);
			clone.setLastDeathMessage(corpse.getLastDeathMessage());
			clone.setSynced(false);
			
			if(DissolutionConfig.respawn.respawnInNether && !DissolutionConfig.respawn.wowLikeRespawn)
				event.getEntityPlayer().setPosition(event.getOriginal().posX, event.getOriginal().posY, event.getOriginal().posZ);
		}
	}

	/**
	 * Makes the player practically invisible to mobs
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onVisibilityPlayer(PlayerEvent.Visibility event) {
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.isIncorporeal())
			event.modifyVisibility(0D);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerAttackEntity(AttackEntityEvent event) {
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative()) {
			if (event.isCancelable())
				event.setCanceled(true);
			return;
		}
		if (event.getTarget() instanceof EntityPlayer) {
			final IIncorporealHandler targetCorp = CapabilityIncorporealHandler.getHandler(event.getTarget());
			if (playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative())
				if (event.isCancelable())
					event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityItemPickup(EntityItemPickupEvent event) {
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
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
			final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity());
			if (playerCorp.isIncorporeal()) {
				playerCorp.setIncorporeal(false);
				/*IMessage msg = new IncorporealMessage(event.getEntity().getUniqueID().getMostSignificantBits(),
						event.getEntity().getUniqueID().getLeastSignificantBits(), playerCorp.isIncorporeal());
				PacketHandler.net.sendToAll(msg);*/
			}
		}
	}

}
