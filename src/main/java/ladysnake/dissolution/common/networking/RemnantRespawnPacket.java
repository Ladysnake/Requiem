package ladysnake.dissolution.common.networking;

import ladylib.LadyLib;
import ladylib.misc.ReflectionUtil;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.client.gui.FlashTransitionEffect;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.DemoPlayerInteractionManager;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.UUID;

public class RemnantRespawnPacket implements IMessageHandler<RemnantRespawnMessage, IMessage> {
    private static MethodHandle playerList$uuidToPlayerMap = ReflectionUtil.findGetterFromObfName(PlayerList.class, "field_177454_f", Map.class);
    private static MethodHandle playerList$setPlayerGameTypeBasedOnOther = ReflectionUtil.findMethodHandleFromObfName(PlayerList.class, "func_72381_a", void.class, EntityPlayerMP.class, EntityPlayerMP.class, World.class);

    @Override
    public IMessage onMessage(RemnantRespawnMessage message, MessageContext ctx) {
        if (ctx.side.isServer()) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ctx.getServerHandler().player.server.addScheduledTask(() -> fakeRespawn(player));
        } else {
            // avoid call to sideonly classes
            FMLClientHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                FlashTransitionEffect.INSTANCE.fade(1);
//                PacketHandler.NET.sendToServer(new RemnantRespawnMessage());
            });
        }
        return null;
    }

    public static void fakeRespawn(EntityPlayerMP player) {
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
        PacketHandler.NET.sendTo(new IncorporealMessage(entityplayermp.getEntityId(), true, SoulStates.SOUL), entityplayermp);
        PacketHandler.NET.sendTo(new FlashTransitionMessage(), entityplayermp);
    }
}
