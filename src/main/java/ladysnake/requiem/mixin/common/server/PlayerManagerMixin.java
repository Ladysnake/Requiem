/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.mixin.common.server;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.minecraft.PlayerCloneCallback;
import ladysnake.requiem.api.v1.event.minecraft.PlayerRespawnCallback;
import ladysnake.requiem.api.v1.event.minecraft.SyncServerResourcesCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static ladysnake.requiem.common.network.RequiemNetworking.createCorporealityMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendTo;
import static ladysnake.requiem.mixin.common.server.PlayerTagKeys.*;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    private static final ThreadLocal<ServerWorld> REQUIEM$RESPAWN_WORLD = new ThreadLocal<>();

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity createdPlayer, CallbackInfo info) {
        sendTo(createdPlayer, createCorporealityMessage(createdPlayer));
    }

    @Shadow @Final private List<ServerPlayerEntity> players;

    @Inject(method = "onPlayerConnect", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/SynchronizeTagsS2CPacket"))
    private void synchronizeServerData(ClientConnection conn, ServerPlayerEntity player, CallbackInfo ci) {
        SyncServerResourcesCallback.EVENT.invoker().onServerSync(player);
    }

    @Inject(method = "onDataPacksReloaded", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/SynchronizeTagsS2CPacket"))
    private void synchronizeServerData(CallbackInfo ci) {
        for (ServerPlayerEntity player : this.players) {
            SyncServerResourcesCallback.EVENT.invoker().onServerSync(player);
        }
    }

    @ModifyVariable(
            method = "onPlayerConnect",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=RootVehicle",
                    ordinal = 0
            ),
            ordinal = 0
    )
    @Nullable
    private CompoundTag logInPossessedEntity(
            @Nullable CompoundTag serializedPlayer,
            ClientConnection connection,
            ServerPlayerEntity player
    ) {
        if (serializedPlayer != null && serializedPlayer.contains(POSSESSED_ROOT_TAG, NbtType.COMPOUND)) {
            sendTo(player, createCorporealityMessage(player));
            ServerWorld world = player.getServerWorld();
            CompoundTag serializedPossessedInfo = serializedPlayer.getCompound(POSSESSED_ROOT_TAG);
            Entity possessedEntityMount = EntityType.loadEntityWithPassengers(
                    serializedPossessedInfo.getCompound(POSSESSED_ENTITY_TAG),
                    world,
                    (entity_1x) -> !world.tryLoadEntity(entity_1x) ? null : entity_1x
            );
            if (possessedEntityMount != null) {
                UUID possessedEntityUuid = serializedPossessedInfo.getUuid(POSSESSED_UUID_TAG);
                resumePossession(((RequiemPlayer) player).asPossessor(), world, possessedEntityMount, possessedEntityUuid);
            }
        }
        return serializedPlayer;
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
            method = "remove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;savePlayerData(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    shift = AFTER,
                    ordinal = 0
            )
    )
    private void logOutPossessedEntity(ServerPlayerEntity player, CallbackInfo info) {
        Entity possessedEntity = ((RequiemPlayer) player).asPossessor().getPossessedEntity();
        if (possessedEntity != null) {
            ServerWorld world = player.getServerWorld();
            world.removeEntity(possessedEntity);
            for (Entity ridden : possessedEntity.getPassengersDeep()) {
                world.removeEntity(ridden);
            }
            world.getChunk(player.chunkX, player.chunkZ).markDirty();
        }
    }

    @ModifyVariable(
        method = "respawnPlayer",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;doesNotCollide(Lnet/minecraft/entity/Entity;)Z")),
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;networkHandler:Lnet/minecraft/server/network/ServerPlayNetworkHandler;",
            ordinal = 0
        ),
        ordinal = 1
    )
    private ServerPlayerEntity firePlayerCloneEvent(ServerPlayerEntity clone, ServerPlayerEntity original, boolean returnFromEnd) {
        PlayerCloneCallback.EVENT.invoker().onPlayerClone(original, clone, returnFromEnd);
        REQUIEM$RESPAWN_WORLD.set(clone.getServerWorld());
        // Prevent players from respawning in fairly bad conditions
        while(!clone.world.doesNotCollide(clone) && clone.getY() < 256.0D) {
            clone.updatePosition(clone.getX(), clone.getY() + 1.0D, clone.getZ());
        }
        return clone;
    }

    @ModifyVariable(
        method = "respawnPlayer",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;doesNotCollide(Lnet/minecraft/entity/Entity;)Z")),
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;networkHandler:Lnet/minecraft/server/network/ServerPlayNetworkHandler;",
            ordinal = 0,
            shift = AFTER
        ),
        ordinal = 1
    )
    private ServerWorld fixRespawnWorld(ServerWorld respawnWorld) {
        return REQUIEM$RESPAWN_WORLD.get();
    }

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void firePlayerRespawnEvent(
            ServerPlayerEntity original,
            boolean returnFromEnd,
            CallbackInfoReturnable<ServerPlayerEntity> cir
    ) {
        PlayerRespawnCallback.EVENT.invoker().onPlayerRespawn(cir.getReturnValue(), returnFromEnd);
    }
}
