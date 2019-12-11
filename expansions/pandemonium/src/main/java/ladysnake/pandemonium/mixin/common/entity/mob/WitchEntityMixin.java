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
package ladysnake.pandemonium.mixin.common.entity.mob;

import ladysnake.pandemonium.common.util.ItemUtil;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(WitchEntity.class)
public class WitchEntityMixin extends HostileEntity {
    protected WitchEntityMixin(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "isDrinking", at = @At("HEAD"), cancellable = true)
    private void isDrinking(CallbackInfoReturnable<Boolean> cir) {
        if (((Possessable)this).isBeingPossessed() && this.getMainHandStack().getItem() != Items.POTION) {
            cir.setReturnValue(false);
        }
    }

    @Nullable
    @ModifyVariable(method = "tickMovement", ordinal = 0, at = @At("STORE"))
    private Potion preventPotionOverride(final Potion selectedPotion) {
        if (((Possessable)this).isBeingPossessed() && !ItemUtil.isWaterBottle(this.getMainHandStack())) {
            return null;
        }
        return selectedPotion;
    }

    @Inject(
            method = "tickMovement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/WitchEntity;equipStack(Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/item/ItemStack;)V",
                    ordinal = 0,
                    shift = AFTER
            )
    )
    private void giveBottleBack(CallbackInfo ci) {
        if (((Possessable)this).isBeingPossessed()) {
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GLASS_BOTTLE));
        }
    }
}
