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
package ladysnake.requiem.mixin.client.render.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.client.RequiemFx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Nullable
    private Entity requiem_camerasPossessed;

    /**
     * Called once per frame, used to update the entity
     */
    @Inject(method = "setRenderPosition", at = @At("HEAD"))
    private void updateCamerasPossessedEntity(double x, double y, double z, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity camera = client.getCameraEntity();
        if (camera instanceof RequiemPlayer) {
            requiem_camerasPossessed = (Entity) ((RequiemPlayer) camera).getPossessionComponent().getPossessedEntity();
            if (requiem_camerasPossessed == null) {
                requiem_camerasPossessed = RequiemFx.INSTANCE.getAnimationEntity();
            }
        } else {
            requiem_camerasPossessed = null;
        }
    }

    /**
     * Prevents the camera's possessed entity from rendering
     */
    @Inject(method = "method_3950", at = @At("HEAD"), cancellable = true)
    private void preventPossessedRender(Entity entity, VisibleRegion visibleRegion, double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        if (requiem_camerasPossessed == entity) {
            info.setReturnValue(false);
        }
    }
}
