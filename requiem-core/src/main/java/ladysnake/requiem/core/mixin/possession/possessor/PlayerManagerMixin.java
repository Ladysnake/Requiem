/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.core.mixin.possession.possessor;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

import static ladysnake.requiem.core.mixin.possession.possessor.PlayerTagKeys.*;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
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
    private NbtCompound logInPossessedEntity(
            @Nullable NbtCompound serializedPlayer,
            ClientConnection connection,
            ServerPlayerEntity player
    ) {
        if (serializedPlayer != null && serializedPlayer.contains(POSSESSED_ROOT_TAG, NbtType.COMPOUND)) {
            RemnantComponent.KEY.sync(player);
            ServerWorld world = player.getWorld();
            NbtCompound serializedPossessedInfo = serializedPlayer.getCompound(POSSESSED_ROOT_TAG);
            Entity possessedEntityMount = EntityType.loadEntityWithPassengers(
                    serializedPossessedInfo.getCompound(POSSESSED_ENTITY_TAG),
                    world,
                    (entity_1x) -> !world.tryLoadEntity(entity_1x) ? null : entity_1x
            );
            if (possessedEntityMount != null) {
                UUID possessedEntityUuid = serializedPossessedInfo.getUuid(POSSESSED_UUID_TAG);
                resumePossession(PossessionComponent.get(player), world, possessedEntityMount, possessedEntityUuid);
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

        if (!player.isPossessionOngoing()) {
            RequiemCore.LOGGER.warn("Couldn't reattach possessed entity to player");
            possessedEntityMount.streamPassengersAndSelf().forEach(e -> e.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
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
        Entity possessedEntity = PossessionComponent.get(player).getHost();
        if (possessedEntity != null) {
            ServerWorld world = player.getWorld();
            possessedEntity.streamPassengersAndSelf().forEach(e -> e.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
        }
    }
}
