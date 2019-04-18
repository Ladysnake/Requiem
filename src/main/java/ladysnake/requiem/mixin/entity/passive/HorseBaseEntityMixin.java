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
package ladysnake.requiem.mixin.entity.passive;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(HorseBaseEntity.class)
public abstract class HorseBaseEntityMixin extends MobEntity {

    protected HorseBaseEntityMixin(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    @Shadow @Nullable public abstract Entity getPrimaryPassenger();

    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    private void undeadHorsesAutoSaddled(CallbackInfoReturnable<Boolean> cir) {
        Entity passenger = this.getPrimaryPassenger();
        if (passenger instanceof RequiemPlayer && this.isUndead()) {
            Possessable possessedEntity = ((RequiemPlayer) passenger).getPossessionComponent().getPossessedEntity();
            if (possessedEntity instanceof LivingEntity && ((LivingEntity) possessedEntity).isUndead()) {
                cir.setReturnValue(true);
            }
        }
    }
}
