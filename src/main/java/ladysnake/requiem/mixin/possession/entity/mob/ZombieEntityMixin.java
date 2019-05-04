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
package ladysnake.requiem.mixin.possession.entity.mob;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin implements Possessable {
    @Inject(method = "convertTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieEntity;remove()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void possessConvertedZombie(EntityType<? extends ZombieEntity> type, CallbackInfo ci, ZombieEntity converted) {
        RequiemPlayer possessor = (RequiemPlayer) this.getPossessor();
        if (possessor != null) {
            PossessionComponent possessionComponent = possessor.getPossessionComponent();
            possessionComponent.stopPossessing(false);
            possessionComponent.startPossessing(converted);
        }
    }
}
