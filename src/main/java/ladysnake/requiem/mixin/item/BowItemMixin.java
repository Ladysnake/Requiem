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
 */
package ladysnake.requiem.mixin.item;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.common.entity.internal.ArrowShooter;
import ladysnake.requiem.mixin.entity.projectile.ProjectileEntityAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends RangedWeaponItem {
    private static final ThreadLocal<LivingEntity> REQUIEM__CURRENT_USER = new ThreadLocal<>();
    public BowItemMixin(Item.Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setCurrentHand(Lnet/minecraft/util/Hand;)V"))
    private void setAttackingMode(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        MobEntity possessed = RequiemPlayer.from(player).asPossessor().getPossessedEntity();
        if (possessed != null) {
            possessed.setAttacking(true);
        }
    }

    @Inject(
            method = "onStoppedUsing",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/entity/player/PlayerAbilities;creativeMode:Z",
                    ordinal = 0
            )
    )
    private void setCurrentUser(ItemStack item, World world, LivingEntity user, int charge, CallbackInfo ci) {
        MobEntity possessed = ((RequiemPlayer) user).asPossessor().getPossessedEntity();
        REQUIEM__CURRENT_USER.set(possessed);
        if (possessed != null) {    // counterpart to setAttackingMode
            possessed.setAttacking(false);
        }
    }

    @ModifyVariable(method = "onStoppedUsing", ordinal = 0, at = @At("STORE"))
    private boolean giveSkeletonInfinity(boolean infinity) {
        if (REQUIEM__CURRENT_USER.get() instanceof AbstractSkeletonEntity) {
            return infinity || RANDOM.nextFloat() < 0.8f;
        }
        return infinity;
    }

    @ModifyVariable(method = "onStoppedUsing", ordinal = 0, at = @At("STORE"))
    private ProjectileEntity useSkeletonArrow(ProjectileEntity firedArrow) {
        LivingEntity entity = REQUIEM__CURRENT_USER.get();
        if (entity instanceof ArrowShooter) {
            return ((ArrowShooter)entity).invokeGetArrow(((ProjectileEntityAccessor)firedArrow).invokeAsItemStack(), 1f);
        }
        return firedArrow;
    }
}
