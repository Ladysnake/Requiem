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
package ladysnake.pandemonium.mixin.common.entity.mob;

import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.common.util.ItemUtil;
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
