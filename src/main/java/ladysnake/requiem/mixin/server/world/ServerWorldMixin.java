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
package ladysnake.requiem.mixin.server.world;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "loadEntityUnchecked", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;loadEntity(Lnet/minecraft/entity/Entity;)V", shift = AFTER))
    private void possessLoadedEntities(Entity entity, CallbackInfo ci) {
        PlayerEntity possessor = ((ProtoPossessable) entity).getPossessor();
        if (possessor != null && entity instanceof MobEntity) {
            ((RequiemPlayer)possessor).asPossessor().startPossessing((MobEntity) entity);
        }
    }
}
