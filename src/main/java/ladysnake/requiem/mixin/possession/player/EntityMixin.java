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
package ladysnake.requiem.mixin.possession.player;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getMaxBreath", at = @At("HEAD"), cancellable = true)
    private void delegateMaxBreath(CallbackInfoReturnable<Integer> cir) {
        if (this instanceof RequiemPlayer) {
            PossessionComponent possessionComponent = ((RequiemPlayer) this).getPossessionComponent();
            // This method can be called in the constructor
            //noinspection ConstantConditions
            if (possessionComponent != null) {
                Entity possessedEntity = possessionComponent.getPossessedEntity();
                if (possessedEntity != null) {
                    cir.setReturnValue(possessedEntity.getMaxBreath());
                }
            }
        }
    }

    @Inject(method = "getBreath", at = @At("HEAD"), cancellable = true)
    private void delegateBreath(CallbackInfoReturnable<Integer> cir) {
        if (this instanceof RequiemPlayer) {
            PossessionComponent possessionComponent = ((RequiemPlayer) this).getPossessionComponent();
            // This method can be called in the constructor
            //noinspection ConstantConditions
            if (possessionComponent != null) {
                Entity possessedEntity = ((RequiemPlayer) this).getPossessionComponent().getPossessedEntity();
                if (possessedEntity != null) {
                    cir.setReturnValue(possessedEntity.getBreath());
                }
            }
        }
    }
}
