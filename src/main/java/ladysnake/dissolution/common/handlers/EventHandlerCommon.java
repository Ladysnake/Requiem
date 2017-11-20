package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.init.ModBlocks;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * This class handles basic events-related logic
 *
 * @author Pyrofab
 */
public class EventHandlerCommon {

    private Map<UUID, double[]> deathPos = new HashMap<>();

    public EventHandlerCommon() {
        LootTableList.register(new ResourceLocation(Reference.MOD_ID, "inject/nether_bridge"));
        LootTableList.register(new ResourceLocation(Reference.MOD_ID, "lament_stone"));
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().toString().equals("minecraft:chests/nether_bridge")) {
            ResourceLocation loc = new ResourceLocation(Reference.MOD_ID, "inject/nether_bridge");
            LootEntry entry = new LootEntryTable(loc, 1, 1, new LootCondition[0], "dissolution_scythe_entry");
            LootPool pool = new LootPool(new LootEntry[]{entry}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1), "dissolution_scythe_pool");
            event.getTable().addPool(pool);
        }
    }

    @SubscribeEvent
    public void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().getItem() == Item.getItemFromBlock(ModBlocks.DEPLETED_COAL))
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
            clone.setCorporealityStatus(DissolutionConfig.respawn.respawnCorporealityStatus);
            clone.getDeathStats().setLastDeathMessage(corpse.getDeathStats().getLastDeathMessage());
            clone.getDialogueStats().deserializeNBT(corpse.getDialogueStats().serializeNBT());
            clone.setSynced(false);

            if (clone.isStrongSoul() && !DissolutionConfig.respawn.wowLikeRespawn) {
                clone.getDeathStats().setDeathDimension(corpse.getDeathStats().getDeathDimension());
                clone.getDeathStats().setDeathLocation(new BlockPos(event.getOriginal().posX, event.getOriginal().posY, event.getOriginal().posZ));
                clone.getDeathStats().setDead(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {
        IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
        // Teleports the player to wherever they should be if needed
        if (playerCorp.getDeathStats().wasDead() && !event.player.world.isRemote) {
            event.player.world.profiler.startSection("placing_respawned_player");
            // changes the player's dimension if required by the config or if they died there
            if (DissolutionConfig.respawn.respawnInNether)
                CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, DissolutionConfig.respawn.respawnDimension);
            else if (!DissolutionConfig.respawn.wowLikeRespawn && event.player.dimension != playerCorp.getDeathStats().getDeathDimension())
                CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, playerCorp.getDeathStats().getDeathDimension());

            // changes the player's position to where they died
            if (!DissolutionConfig.respawn.wowLikeRespawn) {
                BlockPos deathPos = playerCorp.getDeathStats().getDeathLocation();
                if (!event.player.world.isOutsideBuildHeight(deathPos) && event.player.world.isAirBlock(deathPos))
                    ((EntityPlayerMP) event.player).connection.setPlayerLocation(deathPos.getX(), deathPos.getY(), deathPos.getZ(),
                            event.player.rotationYaw, event.player.rotationPitch);
                else if (!event.player.world.isOutsideBuildHeight(deathPos)) {
                    deathPos = getSafeSpawnLocation(event.player.world, deathPos);
                    if (deathPos != null)
                        ((EntityPlayerMP) event.player).connection.setPlayerLocation(deathPos.getX(), deathPos.getY(),
                                deathPos.getZ(), event.player.rotationYaw, event.player.rotationPitch);
                }
            }
            event.player.world.profiler.endSection();
            playerCorp.getDeathStats().setDead(false);
        }
    }

    @Nullable
    private BlockPos getSafeSpawnLocation(World worldIn, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        for (int l = 0; l <= 1; ++l) {
            int i1 = i - 1;
            int j1 = k - 1;
            int k1 = i1 + 2;
            int l1 = j1 + 2;

            for (int i2 = i1; i2 <= k1; ++i2) {
                for (int j2 = j1; j2 <= l1; ++j2) {
                    BlockPos blockpos = new BlockPos(i2, j, j2);

                    if (hasRoomForPlayer(worldIn, blockpos)) {
                        return blockpos;
                    }
                }
            }
        }
        return null;
    }

    protected boolean hasRoomForPlayer(World worldIn, BlockPos pos) {
        return !worldIn.getBlockState(pos).getMaterial().isSolid() && !worldIn.getBlockState(pos.up()).getMaterial().isSolid();
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof EntityPlayer && !event.getSource().canHarmInCreative()) {
            IIncorporealHandler.CorporealityStatus status = CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity()).getCorporealityStatus();
            if (status.isIncorporeal()) {
                if (event.getSource().getTrueSource() == null || !DissolutionConfigManager.canEctoplasmBeAttackedBy(event.getSource().getTrueSource()))
                    event.setCanceled(!event.getSource().canHarmInCreative());
            }
        } else {
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getSource().getTrueSource());
            if (handler != null && handler.getPossessed() != null)
                if (handler.getPossessed().proxyAttack(event.getEntityLiving(), event.getSource(), event.getAmount()))
                    event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntity(AttackEntityEvent event) {
        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (playerCorp.getCorporealityStatus().isIncorporeal() && !event.getEntityPlayer().isCreative()) {
            if (playerCorp.getPossessed() instanceof EntityLiving && !((EntityLiving) playerCorp.getPossessed()).isDead) {
                if (event.getTarget() instanceof EntityLivingBase)
                    event.getEntityPlayer().getHeldItemMainhand().hitEntity((EntityLivingBase) event.getTarget(), event.getEntityPlayer());
                ((EntityLiving) playerCorp.getPossessed()).attackEntityAsMob(event.getTarget());
            } else if (!DissolutionConfigManager.canEctoplasmBeAttackedBy(event.getTarget()))
                event.setCanceled(true);
            return;
        }
        if (event.getTarget() instanceof EntityPlayer) {
            final IIncorporealHandler targetCorp = CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getTarget());
            if (targetCorp.getCorporealityStatus().isIncorporeal() && !event.getEntityPlayer().isCreative())
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getTarget());
        if (handler != null && event.getEntity() instanceof EntityLiving && handler.getCorporealityStatus().isIncorporeal() && !DissolutionConfigManager.canEctoplasmBeAttackedBy(event.getEntity()))
            ((EntityLiving) event.getEntity()).setAttackTarget(null);
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
            final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity());
            if (playerCorp.getCorporealityStatus().isIncorporeal()) {
                playerCorp.setCorporealityStatus(IIncorporealHandler.CorporealityStatus.BODY);
            }
        }
    }

}
