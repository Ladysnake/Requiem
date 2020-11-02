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
package ladysnake.requiem.mixin.client.render.entity;

import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.client.RequiemFx;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @SuppressWarnings("UnresolvedMixinReference")   // Minecraft dev plugin is an idiot sandwich
    @Nullable
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getVehicle()Lnet/minecraft/entity/Entity;"))
    private Entity getPossessorRiddenEntity(LivingEntity entity) {
        PlayerEntity possessor = ((Possessable) entity).getPossessor();
        if (possessor != null) {
            return possessor.getVehicle();
        }
        return entity.getVehicle();
    }

    @SuppressWarnings("UnresolvedMixinReference")   // Minecraft dev plugin is an idiot sandwich
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasVehicle()Z"))
    private boolean doesPossessorHaveVehicle(LivingEntity entity) {
        PlayerEntity possessor = ((Possessable) entity).getPossessor();
        if (possessor != null) {
            return possessor.hasVehicle();
        }
        return entity.hasVehicle();
    }

    @Inject(method = "getRenderLayer", at = @At("RETURN"), cancellable = true)
    protected void requiem$replaceRenderLayer(T entity, boolean showBody, boolean translucent, boolean bl, CallbackInfoReturnable<RenderLayer> cir) {
        RequiemFx requiemFxRenderer = RequiemClient.INSTANCE.getRequiemFxRenderer();
        if (entity == requiemFxRenderer.getAnimationEntity()) {
            cir.setReturnValue(requiemFxRenderer.getZoomFx(cir.getReturnValue()));
        }
    }
}
