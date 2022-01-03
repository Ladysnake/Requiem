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
package ladysnake.requiem.mixin.client.possession;

import ladysnake.requiem.api.v1.event.requiem.client.RenderSelfPossessedEntityCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Shadow
    public Camera camera;
    @Nullable
    private Entity requiem_camerasPossessed;

    /**
     * Called once per frame, used to update the entity
     */
    @Inject(method = "configure", at = @At("HEAD"))
    private void updateCamerasPossessedEntity(World w, Camera c, Entity e, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity camera = client.getCameraEntity();
        requiem_camerasPossessed = camera == null ? null : PossessionComponent.getHost(camera);
    }

    /**
     * Prevents the camera's possessed entity from rendering
     */
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void preventPossessedRender(Entity entity, Frustum visibleRegion, double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        if (requiem_camerasPossessed == entity) {
            if (camera.isThirdPerson() || !RenderSelfPossessedEntityCallback.EVENT.invoker().allowRender(entity)) {
                info.setReturnValue(false);
            }
        }
    }

    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
    private static void preventShadowRender(MatrixStack matrices, VertexConsumerProvider vertices, Entity rendered, float distance, float tickDelta, WorldView world, float radius, CallbackInfo ci) {
        if (RemnantComponent.isVagrant(rendered)) {
            ci.cancel();
        }
    }
}
