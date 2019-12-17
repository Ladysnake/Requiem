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

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

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
}
