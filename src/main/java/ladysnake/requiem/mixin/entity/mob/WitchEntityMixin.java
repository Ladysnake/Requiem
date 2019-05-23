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
package ladysnake.requiem.mixin.entity.mob;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.thrown.ThrownPotionEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;
import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILSOFT;

@Mixin(WitchEntity.class)
public abstract class WitchEntityMixin {
    @Inject(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/thrown/ThrownPotionEntity;setItemStack(Lnet/minecraft/item/ItemStack;)V", shift = AFTER),
            locals = CAPTURE_FAILSOFT
    )
    private void makeWitchesThrowWeaknessAtUndead(
            LivingEntity target,
            float charge,
            CallbackInfo ci,
            Vec3d targetVelocity,
            double dx,
            double dy,
            double dz,
            float horizontalDistance,
            Potion potion,
            ThrownPotionEntity thrownPotion
    ) {
        if (target.isUndead() && !target.hasStatusEffect(StatusEffects.WEAKNESS)) {
            PotionUtil.setPotion(thrownPotion.getStack(), Potions.WEAKNESS);
        }
    }
}
