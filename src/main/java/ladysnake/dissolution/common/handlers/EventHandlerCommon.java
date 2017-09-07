package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;


/**
 * This class handles basic events-related logic
 * It is mostly used to cancel player interactions when the latter is a ghost
 * @author Pyrofab
 *
 */
public class EventHandlerCommon {
	
	public EventHandlerCommon() {
		ResourceLocation loc = new ResourceLocation(Reference.MOD_ID, "inject/nether_bridge");
		LootTableList.register(loc);
	}

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
	public void onLootTableLoad(LootTableLoadEvent event) {
		if (event.getName().toString().equals("minecraft:chests/nether_bridge")) {
			ResourceLocation loc = new ResourceLocation(Reference.MOD_ID, "inject/nether_bridge");
			System.out.println("Editing loot tables");
			LootEntry entry = new LootEntryTable(loc, 1, 1, new LootCondition[0], "dissolution_scythe_entry");
			LootPool pool = new LootPool(new LootEntry[] { entry }, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1), "dissolution_scythe_pool");
			event.getTable().addPool(pool);
	    }
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		event.player.inventoryContainer.addListener(new PlayerInventoryListener((EntityPlayerMP) event.player));
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
