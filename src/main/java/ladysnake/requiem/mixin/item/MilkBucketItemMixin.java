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
package ladysnake.requiem.mixin.item;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin {
    @Inject(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z", shift = AFTER))
    private void regenSkeletons(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof RequiemPlayer) {
            LivingEntity possessed = ((RequiemPlayer) user).asPossessor().getPossessedEntity();
            if (possessed != null && RequiemEntityTypeTags.SKELETONS.contains(possessed.getType())) {
                possessed.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 30*20));
            }
        }
    }
}
