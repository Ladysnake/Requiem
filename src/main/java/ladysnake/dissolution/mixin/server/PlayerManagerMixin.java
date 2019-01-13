package ladysnake.dissolution.mixin.server;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.possession.Possessable;
import ladysnake.dissolution.api.possession.Possessor;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;
import net.minecraft.world.ChunkSaveHandlerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createCorporealityPacket;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendTo;
import static ladysnake.dissolution.mixin.server.PlayerTagKeys.*;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity createdPlayer, CallbackInfo info) {
        sendTo(createdPlayer, createCorporealityPacket(createdPlayer));
    }

    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=RootVehicle",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void logInPossessedEntity(
            ClientConnection connection,
            ServerPlayerEntity player,
            CallbackInfo info,
            // Local variables
            GameProfile gameProfile_1,
            UserCache userCache_1,
            String string_1,
            CompoundTag serializedPlayer
    ) {
        if (serializedPlayer != null && serializedPlayer.containsKey(POSSESSED_ROOT_TAG, NbtType.COMPOUND)) {
            ServerWorld world = this.server.getWorld(player.dimension);
            CompoundTag serializedPossessedInfo = serializedPlayer.getCompound(POSSESSED_ROOT_TAG);
            Entity possessedEntityMount = ChunkSaveHandlerImpl.readEntity(serializedPossessedInfo.getCompound(POSSESSED_ENTITY_TAG), world, true);
            if (possessedEntityMount != null) {
                UUID possessedEntityUuid = serializedPossessedInfo.getUuid(POSSESSED_UUID_TAG);
                resumePossession((Possessor) player, world, possessedEntityMount, possessedEntityUuid);
            }
        }
    }

    private void resumePossession(Possessor player, ServerWorld world, Entity possessedEntityMount, UUID possessedEntityUuid) {
        if (possessedEntityMount instanceof MobEntity && possessedEntityMount.getUuid().equals(possessedEntityUuid)) {
            player.startPossessing((MobEntity) possessedEntityMount);
        } else {
            for (Entity entity : possessedEntityMount.method_5736()) {
                if (entity instanceof MobEntity && entity.getUuid().equals(possessedEntityUuid)) {
                    player.startPossessing((MobEntity) entity);
                    break;
                }
            }
        }

        if (!player.isPossessing()) {
            Dissolution.LOGGER.warn("Couldn't reattach possessed entity to player");
            world.method_8507(possessedEntityMount);

            for (Entity entity : possessedEntityMount.method_5736()) {
                world.method_8507(entity);
            }
        }
    }

    @Inject(
            method = "method_14611",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;savePlayerData(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    shift = AFTER,
                    ordinal = 0
            )
    )
    public void logOutPossessedEntity(ServerPlayerEntity player, CallbackInfo info) {
        Possessable possessedEntity = ((Possessor) player).getPossessedEntity();
        if (possessedEntity != null) {
            ((Possessor) player).stopPossessing();
            ServerWorld serverWorld_1 = player.getServerWorld();
            serverWorld_1.method_8507((Entity) possessedEntity);
            for (Entity ridden : ((Entity) possessedEntity).method_5736()) {
                serverWorld_1.method_8507(ridden);
            }
            serverWorld_1.getChunk(player.chunkX, player.chunkZ).markDirty();
        }
    }
}
