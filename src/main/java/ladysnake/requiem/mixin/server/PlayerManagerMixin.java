/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.mixin.server;

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.minecraft.PlayerCloneCallback;
import ladysnake.requiem.api.v1.event.minecraft.PlayerRespawnCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.UUID;

import static ladysnake.requiem.common.network.RequiemNetworking.createCorporealityMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendTo;
import static ladysnake.requiem.mixin.server.PlayerTagKeys.*;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity createdPlayer, CallbackInfo info) {
        sendTo(createdPlayer, createCorporealityMessage(createdPlayer));
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
    private void logInPossessedEntity(
            ClientConnection connection,
            ServerPlayerEntity player,
            CallbackInfo info,
            // Local variables
            GameProfile gameProfile_1,
            UserCache userCache_1,
            String string_1,
            @Nullable CompoundTag serializedPlayer
    ) {
        if (serializedPlayer != null && serializedPlayer.containsKey(POSSESSED_ROOT_TAG, NbtType.COMPOUND)) {
            sendTo(player, createCorporealityMessage(player));
            ServerWorld world = this.server.getWorld(player.dimension);
            CompoundTag serializedPossessedInfo = serializedPlayer.getCompound(POSSESSED_ROOT_TAG);
            Entity possessedEntityMount = EntityType.loadEntityWithPassengers(
                    serializedPossessedInfo.getCompound(POSSESSED_ENTITY_TAG),
                    world,
                    (entity_1x) -> !world.method_18768(entity_1x) ? null : entity_1x
            );
            if (possessedEntityMount != null) {
                UUID possessedEntityUuid = serializedPossessedInfo.getUuid(POSSESSED_UUID_TAG);
                resumePossession(((RequiemPlayer) player).getPossessionComponent(), world, possessedEntityMount, possessedEntityUuid);
            }
        }
    }

    private void resumePossession(PossessionComponent player, ServerWorld world, Entity possessedEntityMount, UUID possessedEntityUuid) {
        if (possessedEntityMount instanceof MobEntity && possessedEntityMount.getUuid().equals(possessedEntityUuid)) {
            player.startPossessing((MobEntity) possessedEntityMount);
        } else {
            for (Entity entity : possessedEntityMount.getPassengersDeep()) {
                if (entity instanceof MobEntity && entity.getUuid().equals(possessedEntityUuid)) {
                    player.startPossessing((MobEntity) entity);
                    break;
                }
            }
        }

        if (!player.isPossessing()) {
            Requiem.LOGGER.warn("Couldn't reattach possessed entity to player");
            world.removeEntity(possessedEntityMount);

            for (Entity entity : possessedEntityMount.getPassengersDeep()) {
                world.removeEntity(entity);
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
    private void logOutPossessedEntity(ServerPlayerEntity player, CallbackInfo info) {
        Entity possessedEntity = ((RequiemPlayer) player).getPossessionComponent().getPossessedEntity();
        if (possessedEntity != null) {
            ServerWorld world = player.getServerWorld();
            world.removeEntity(possessedEntity);
            for (Entity ridden : possessedEntity.getPassengersDeep()) {
                world.removeEntity(ridden);
            }
            world.method_8497(player.chunkX, player.chunkZ).markDirty();
        }
    }

    @Inject(
            method = "respawnPlayer",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;doesNotCollide(Lnet/minecraft/entity/Entity;)Z")),
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;networkHandler:Lnet/minecraft/server/network/ServerPlayNetworkHandler;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void firePlayerCloneEvent(
            ServerPlayerEntity original,
            DimensionType destination,
            boolean returnFromEnd,
            CallbackInfoReturnable<ServerPlayerEntity> cir,
            BlockPos spawnPos,
            boolean forcedSpawn,
            ServerPlayerInteractionManager manager,
            ServerPlayerEntity clone
    ) {
        PlayerCloneCallback.EVENT.invoker().onPlayerClone(original, clone, returnFromEnd);
    }

    @Inject(
            method = "respawnPlayer",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;doesNotCollide(Lnet/minecraft/entity/Entity;)Z")),
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void firePlayerRespawnEvent(
            ServerPlayerEntity original,
            DimensionType destination,
            boolean returnFromEnd,
            CallbackInfoReturnable<ServerPlayerEntity> cir,
            BlockPos spawnPos,
            boolean forcedSpawn,
            ServerPlayerInteractionManager manager,
            ServerPlayerEntity clone
    ) {
        PlayerRespawnCallback.EVENT.invoker().onPlayerRespawn(clone, returnFromEnd);
    }
}
