package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.init.ModBlocks;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;


/**
 * This class handles basic events-related logic
 * @author Pyrofab
 *
 */
public class EventHandlerCommon {

	public EventHandlerCommon() {
		LootTableList.register(new ResourceLocation(Reference.MOD_ID, "inject/nether_bridge"));
		LootTableList.register(new ResourceLocation(Reference.MOD_ID, "lament_stone"));
	}
	
	@SubscribeEvent
	public void onLootTableLoad(LootTableLoadEvent event) {
		if (event.getName().toString().equals("minecraft:chests/nether_bridge")) {
			ResourceLocation loc = new ResourceLocation(Reference.MOD_ID, "inject/nether_bridge");
			LootEntry entry = new LootEntryTable(loc, 1, 1, new LootCondition[0], "dissolution_scythe_entry");
			LootPool pool = new LootPool(new LootEntry[] { entry }, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1), "dissolution_scythe_pool");
			event.getTable().addPool(pool);
	    }
	}

	@SubscribeEvent
	public void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
		if(event.getItemStack().getItem() == Item.getItemFromBlock(ModBlocks.DEPLETED_COAL))
			event.setBurnTime(12000);
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
			clone.setStrongSoul(corpse.isStrongSoul());
			clone.setCorporealityStatus(IIncorporealHandler.CorporealityStatus.SOUL);
			clone.setLastDeathMessage(corpse.getLastDeathMessage());
			clone.setSynced(false);
			
			if(DissolutionConfig.respawn.respawnInNether && !DissolutionConfig.respawn.wowLikeRespawn)
				event.getEntityPlayer().setPosition(event.getOriginal().posX, event.getOriginal().posY, event.getOriginal().posZ);
		}
	}

	/**
	 * Makes the player practically invisible to mobs
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onVisibilityPlayer(PlayerEvent.Visibility event) {
		final IIncorporealHandler.CorporealityStatus playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer()).getCorporealityStatus();
		if (playerCorp.isIncorporeal())
			event.modifyVisibility(0D);
	}
	
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onLivingAttack(LivingAttackEvent event) {
		if(event.getEntity() instanceof EntityPlayer) {
			IIncorporealHandler.CorporealityStatus status = CapabilityIncorporealHandler.getHandler((EntityPlayer)event.getEntity()).getCorporealityStatus();
			if(status.isIncorporeal())
				event.setCanceled(!event.getSource().canHarmInCreative());
		} else {
			IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getSource().getTrueSource());
			if(handler != null && handler.getPossessed() != null)
				if(handler.getPossessed().proxyAttack(event.getEntityLiving(), event.getSource(), event.getAmount()))
					event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerAttackEntity(AttackEntityEvent event) {
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.getCorporealityStatus().isIncorporeal() && !event.getEntityPlayer().isCreative()) {
			if(playerCorp.getPossessed() instanceof EntityLiving)
				((EntityLiving) playerCorp.getPossessed()).attackEntityAsMob(event.getTarget());
			event.setCanceled(true);
			return;
		}
		if (event.getTarget() instanceof EntityPlayer) {
			final IIncorporealHandler targetCorp = CapabilityIncorporealHandler.getHandler((EntityPlayer)event.getTarget());
			if (targetCorp.getCorporealityStatus().isIncorporeal() && !event.getEntityPlayer().isCreative())
				event.setCanceled(true);
		}
	}

/*
	@SubscribeEvent
	public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
		if (event.getEntityLiving() instanceof EntityMob && (!event.getEntityLiving().isEntityUndead() || !event.getEntityLiving().isNonBoss())) {
			EntityMob mob = (EntityMob) event.getEntityLiving();
			if(mob.world.getMinecraftServer().getPlayerList().getPlayers().stream()
					.anyMatch(entityPlayerMP -> CapabilityIncorporealHandler.getHandler(entityPlayerMP).getCorporealityStatus().isIncorporeal())
					&& mob.targetTasks.taskEntries.stream().anyMatch(this::isMobTargetingPlayer))
				mob.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(mob, AbstractMinion.class, true));
		}
	}

	private boolean isMobTargetingPlayer(EntityAITasks.EntityAITaskEntry task) {
		if (task.action instanceof EntityAINearestAttackableTarget) {
			try {
				Class<?> clazz = (Class<?>) entityAINearestAttackableTarget$targetClass.invoke(task.action);
				return clazz == EntityPlayer.class || clazz == EntityPlayerMP.class;
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}
		return false;
	}
*/

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityItemPickup(EntityItemPickupEvent event) {
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.getCorporealityStatus().isIncorporeal() && playerCorp.getPossessed() == null && !event.getEntityPlayer().isCreative())
			event.setCanceled(true);
	}

	/**
	 * Makes the players tangible again when stroke by lightning. Just because we can.
	 */
	@SubscribeEvent
	public void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
		if (event.getEntity() instanceof EntityPlayer) {
			final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler((EntityPlayer)event.getEntity());
			if (playerCorp.getCorporealityStatus().isIncorporeal()) {
				playerCorp.setCorporealityStatus(IIncorporealHandler.CorporealityStatus.BODY);
			}
		}
	}

}
