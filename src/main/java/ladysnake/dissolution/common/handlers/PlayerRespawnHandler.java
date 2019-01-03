package ladysnake.dissolution.common.handlers;

import ladylib.LadyLib;
import ladylib.misc.ReflectionUtil;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.networking.FlashTransitionMessage;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.DemoPlayerInteractionManager;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerRespawnHandler {

    private static MethodHandle destroyVanishingCursedItems = ReflectionUtil.findMethodHandleFromObfName(EntityPlayer.class, "func_190776_cN", void.class);
    private static MethodHandle entity$setFlag = ReflectionUtil.findMethodHandleFromObfName(Entity.class, "func_70052_a", void.class, int.class, boolean.class);
    private static MethodHandle playerList$uuidToPlayerMap = ReflectionUtil.findGetterFromObfName(PlayerList.class, "field_177454_f", Map.class);
    private static MethodHandle playerList$setPlayerGameTypeBasedOnOther = ReflectionUtil.findMethodHandleFromObfName(PlayerList.class, "func_72381_a", void.class, EntityPlayerMP.class, EntityPlayerMP.class, World.class);


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            this.handlePlayerDeath(event);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
        // Teleports the player to wherever they should be if needed
        if (!event.isEndConquered() && !event.player.world.isRemote) {
            playerCorp.setCorporealityStatus(SoulStates.SOUL);
            // Don't bother if placement has already been taken care of in PlayerRespawnHandler#fakeRespawn
            if (!Dissolution.config.respawn.skipDeathScreen) {
                placeRespawnedPlayer(event, playerCorp);
            }
        }
    }

    private void placeRespawnedPlayer(PlayerEvent.PlayerRespawnEvent event, IIncorporealHandler playerCorp) {
        event.player.world.profiler.startSection("placing_respawned_player");
        // changes the player's dimension if required by the config or if they died there
        if (Dissolution.config.respawn.respawnInNether) {
            CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, Dissolution.config.respawn.respawnDimension);
        } else if (event.player.dimension != playerCorp.getDeathStats().getDeathDimension()) {
            CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, playerCorp.getDeathStats().getDeathDimension());
        }

        // changes the player's position to where they died
        BlockPos deathPos = playerCorp.getDeathStats().getDeathLocation();
        if (!event.player.world.isOutsideBuildHeight(deathPos) && event.player.world.isAirBlock(deathPos)) {
            ((EntityPlayerMP) event.player).connection.setPlayerLocation(deathPos.getX(), deathPos.getY(), deathPos.getZ(),
                    event.player.rotationYaw, event.player.rotationPitch);
        } else if (!event.player.world.isOutsideBuildHeight(deathPos)) {
            deathPos = getSafeSpawnLocation(event.player.world, deathPos);
            if (deathPos != null) {
                ((EntityPlayerMP) event.player).connection.setPlayerLocation(deathPos.getX(), deathPos.getY(),
                        deathPos.getZ(), event.player.rotationYaw, event.player.rotationPitch);
            }
        }
        event.player.world.profiler.endSection();
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

    private void handlePlayerDeath(LivingDeathEvent event) {
        final EntityPlayer p = (EntityPlayer) event.getEntity();
        final IIncorporealHandler corp = CapabilityIncorporealHandler.getHandler(p);
        if (corp.getCorporealityStatus().isIncorporeal() && corp.getPossessed() != null) {
            corp.getPossessed().attackEntityFrom(event.getSource(), Float.MAX_VALUE);
            corp.setPossessed(null);
        }
        if (!corp.isStrongSoul() || corp.getCorporealityStatus() == SoulStates.SOUL) {
            return;
        }
        if (corp.getCorporealityStatus() == SoulStates.ECTOPLASM) {
            corp.setCorporealityStatus(SoulStates.SOUL);
            p.setHealth(20f);
            event.setCanceled(true);
            return;
        }

        corp.getDeathStats().setDeathDimension(p.dimension);

        if (p.world instanceof WorldServer) {
            Entity killer = event.getSource().getTrueSource();
            LootTable table = p.world.getLootTableManager().getLootTableFromLocation(new ResourceLocation("dissolution:inject/human"));
            LootContext.Builder ctx = new LootContext.Builder((WorldServer) p.world)
                    .withLootedEntity(p) // set looted entity
                    .withDamageSource(event.getSource()); // pass killing blow and non-player killer
            if (killer instanceof EntityPlayer) {
                ctx = ctx
                        .withLuck(((EntityPlayer) killer).getLuck()) // adjust luck, commonly EntityPlayer.getLuck()
                        .withPlayer((EntityPlayer) killer); // set player as killer
            }
            List<ItemStack> stacks = table.generateLootForPools(p.world.rand, ctx.build());
            for (ItemStack stack : stacks) {
                p.dropItem(stack, true, true);
            }
        }

        if (Dissolution.config.respawn.skipDeathScreen) {
            if (!p.world.isRemote) {
                EntityPlayerMP playerMP = (EntityPlayerMP) p;
                fakePlayerDeath(playerMP, event.getSource());
                PacketHandler.NET.sendTo(new FlashTransitionMessage(), playerMP);
                fakeRespawn(playerMP);
            }
            event.setCanceled(true);
        }
    }

    public static void fakePlayerDeath(EntityPlayerMP player, DamageSource cause) {
        boolean flag = player.world.getGameRules().getBoolean("showDeathMessages");

        if (flag) {
            Team team = player.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    player.server.getPlayerList().sendMessageToAllTeamMembers(player, player.getCombatTracker().getDeathMessage());
                } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    player.server.getPlayerList().sendMessageToTeamOrAllPlayers(player, player.getCombatTracker().getDeathMessage());
                }
            } else {
                player.server.getPlayerList().sendMessage(player.getCombatTracker().getDeathMessage());
            }
        }

        if (!player.world.getGameRules().getBoolean("keepInventory") && !player.isSpectator()) {
            player.captureDrops = true;
            player.capturedDrops.clear();
            try {
                destroyVanishingCursedItems.invoke(player);
            } catch (Throwable t) {
                LadyLib.LOGGER.error("Failed to destroy vanishing items", t);
            }
            player.inventory.dropAllItems();

            net.minecraftforge.event.entity.player.PlayerDropsEvent event = new net.minecraftforge.event.entity.player.PlayerDropsEvent(player, cause, player.capturedDrops, false);
            if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
                for (net.minecraft.entity.item.EntityItem item : player.capturedDrops) {
                    player.world.spawnEntity(item);
                }
            }
        }

        for (ScoreObjective scoreobjective : player.world.getScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT)) {
            Score score = player.getWorldScoreboard().getOrCreateScore(player.getName(), scoreobjective);
            score.incrementScore();
        }

        EntityLivingBase entitylivingbase = player.getAttackingEntity();

        if (entitylivingbase != null) {
            EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entitylivingbase));

            if (entitylist$entityegginfo != null) {
                player.addStat(entitylist$entityegginfo.entityKilledByStat);
            }

        }

        player.clearActivePotions();
        player.addStat(StatList.DEATHS);
        player.takeStat(StatList.TIME_SINCE_DEATH);
        player.extinguish();
        try {
            entity$setFlag.invoke(player, 0, false);
        } catch (Throwable throwable) {
            Dissolution.LOGGER.error("Could not set death flag", throwable);
        }
        player.getCombatTracker().reset();
    }

    private void fakeRespawn(EntityPlayerMP player) {
        IIncorporealHandler corp = CapabilityIncorporealHandler.getHandler(player);
        MinecraftServer server = player.server;
        int dimension = player.dimension;
        PlayerList list = server.getPlayerList();
        // Just in case
        if (!corp.isStrongSoul()) {
            player.connection.processClientStatus(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
            return;
        }

        if (player.getHealth() > 0.0F) {
            return;
        }

        World world = server.getWorld(dimension);
        if (world == null) {
            dimension = player.getSpawnDimension();
        }
        if (server.getWorld(dimension) == null) dimension = 0;

        player.getServerWorld().getEntityTracker().removePlayerFromTrackers(player);
        player.getServerWorld().getEntityTracker().untrack(player);
        player.getServerWorld().getPlayerChunkMap().removePlayer(player);
        list.getPlayers().remove(player);
        server.getWorld(player.dimension).removeEntityDangerously(player);
        BlockPos blockpos = player.getBedLocation(dimension);
        boolean flag = player.isSpawnForced(dimension);
        player.dimension = dimension;
        PlayerInteractionManager playerinteractionmanager;

        if (server.isDemo()) {
            playerinteractionmanager = new DemoPlayerInteractionManager(server.getWorld(player.dimension));
        } else {
            playerinteractionmanager = new PlayerInteractionManager(server.getWorld(player.dimension));
        }

        EntityPlayerMP entityplayermp = new EntityPlayerMP(server, server.getWorld(player.dimension), player.getGameProfile(), playerinteractionmanager);
        entityplayermp.connection = player.connection;
        entityplayermp.copyFrom(player, false);
        entityplayermp.dimension = dimension;
        entityplayermp.setEntityId(player.getEntityId());
        entityplayermp.setCommandStats(player);
        entityplayermp.setPrimaryHand(player.getPrimaryHand());
        // CHANGE: respawned remnants spawn directly at the location of the old one
        entityplayermp.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

        for (String s : player.getTags()) {
            entityplayermp.addTag(s);
        }

        WorldServer worldserver = server.getWorld(player.dimension);
        try {
            playerList$setPlayerGameTypeBasedOnOther.invoke(list, entityplayermp, player, worldserver);
        } catch (Throwable throwable) {
            LadyLib.LOGGER.error("Could not set respawned player's gamemode", throwable);
        }

        if (blockpos != null) {
            BlockPos blockpos1 = EntityPlayer.getBedSpawnLocation(server.getWorld(player.dimension), blockpos, flag);

            if (blockpos1 != null) {
/*
                entityplayermp.setLocationAndAngles((double) ((float) blockpos1.getX() + 0.5F), (double) ((float) blockpos1.getY() + 0.1F), (double) ((float) blockpos1.getZ() + 0.5F), 0.0F, 0.0F);
*/
                entityplayermp.setSpawnPoint(blockpos, flag);
            }/* else {
                entityplayermp.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
            }*/
        }

        worldserver.getChunkProvider().provideChunk((int) entityplayermp.posX >> 4, (int) entityplayermp.posZ >> 4);

        while (!worldserver.getCollisionBoxes(entityplayermp, entityplayermp.getEntityBoundingBox()).isEmpty() && entityplayermp.posY < 256.0D) {
            entityplayermp.setPosition(entityplayermp.posX, entityplayermp.posY + 1.0D, entityplayermp.posZ);
        }

//            entityplayermp.connection.sendPacket(new SPacketRespawn(entityplayermp.dimension, entityplayermp.world.getDifficulty(), entityplayermp.world.getWorldInfo().getTerrainType(), entityplayermp.interactionManager.getGameType()));
//            BlockPos blockpos2 = worldserver.getSpawnPoint();
//            entityplayermp.connection.setPlayerLocation(entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
//            entityplayermp.connection.sendPacket(new SPacketSpawnPosition(blockpos2));
//            entityplayermp.connection.sendPacket(new SPacketSetExperience(entityplayermp.experience, entityplayermp.experienceTotal, entityplayermp.experienceLevel));
        list.updateTimeAndWeatherForPlayer(entityplayermp, worldserver);
        list.updatePermissionLevel(entityplayermp);
        worldserver.getPlayerChunkMap().addPlayer(entityplayermp);
        worldserver.spawnEntity(entityplayermp);
        list.getPlayers().add(entityplayermp);
        try {
            @SuppressWarnings("unchecked") Map<UUID, EntityPlayerMP> uuidToPlayerMap = (Map<UUID, EntityPlayerMP>) playerList$uuidToPlayerMap.invoke(list);
            uuidToPlayerMap.put(entityplayermp.getUniqueID(), entityplayermp);
        } catch (Throwable throwable) {
            LadyLib.LOGGER.error("Could not access UUID to EntityPlayerMP map in PlayerList", throwable);
        }
        entityplayermp.addSelfToInternalCraftingInventory();
        entityplayermp.setHealth(entityplayermp.getHealth());
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerRespawnEvent(entityplayermp, false);
        player.connection.player = entityplayermp;
        // Try to fix the "all entities disappear" bug
        player.getServerWorld().getEntityTracker().updateVisibility(entityplayermp);
        PacketHandler.NET.sendTo(new IncorporealMessage(entityplayermp.getEntityId(), true, SoulStates.SOUL), entityplayermp);
        PacketHandler.NET.sendTo(new FlashTransitionMessage(), entityplayermp);
    }

}
