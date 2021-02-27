/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.pandemonium.mixin.common.server.world;

import ladysnake.pandemonium.common.entity.fakeplayer.FakePlayerEntity;
import ladysnake.pandemonium.common.entity.fakeplayer.RequiemFakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {
    private static final ThreadLocal<List<WeakReference<Entity>>> requiem$rejectedEntities = ThreadLocal.withInitial(ArrayList::new);

    @Shadow
    @Final
    private ServerWorld world;

    @Dynamic("Lambda method")
    @ModifyVariable(method = "method_17227", at = @At("STORE"))
    private Entity loadFakePlayers(Entity entity, ChunkHolder tmp, Chunk chunk) {
        if (entity instanceof RequiemFakePlayer && !this.world.loadEntity(entity)) {
            requiem$rejectedEntities.get().add(new WeakReference<>(entity));
        }
        return entity;
    }

    @Dynamic("Lambda method")
    @Inject(method = "method_17227", at = @At("RETURN"))
    private void removeRejectedPlayers(ChunkHolder tmp, Chunk chunk, CallbackInfoReturnable<Chunk> cir) {
        List<WeakReference<Entity>> rejectedEntities = requiem$rejectedEntities.get();
        WorldChunk ret = (WorldChunk) cir.getReturnValue();
        for (WeakReference<Entity> ref : rejectedEntities) {
            Entity entity = ref.get();
            if (entity != null) {
                ret.remove(entity);
            }
        }
    }

    @Inject(method = "handlePlayerAddedOrRemoved", at = @At("HEAD"), cancellable = true)
    private void handleFakePlayerAddedOrRemoved(ServerPlayerEntity player, boolean added, CallbackInfo ci) {
        if (added && player instanceof FakePlayerEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "updateCameraPosition", at = @At("HEAD"), cancellable = true)
    private void updateFakeCameraPosition(ServerPlayerEntity player, CallbackInfo ci) {
        if (player instanceof FakePlayerEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "doesNotGenerateChunks", at = @At("RETURN"), cancellable = true)
    private void doesNotGenerateChunks(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && player instanceof FakePlayerEntity) {
            cir.setReturnValue(true);
        }
    }
}
