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
package ladysnake.requiem.mixin.common.item;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.mixin.common.entity.mob.ArrowShooter;
import ladysnake.requiem.mixin.common.entity.projectile.ProjectileEntityAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
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
    private boolean giveSkeletonInfinity(boolean infinity, ItemStack item, World world, LivingEntity user, int charge) {
        MobEntity possessed = ((RequiemPlayer) user).asPossessor().getPossessedEntity();
        if (possessed instanceof AbstractSkeletonEntity) {
            return infinity || RANDOM.nextFloat() < 0.8f;
        }
        return infinity;
    }

    @ModifyVariable(method = "onStoppedUsing", ordinal = 0, at = @At("STORE"))
    private PersistentProjectileEntity useSkeletonArrow(PersistentProjectileEntity firedArrow, ItemStack item, World world, LivingEntity user, int charge) {
        LivingEntity possessed = ((RequiemPlayer) user).asPossessor().getPossessedEntity();
        if (possessed instanceof ArrowShooter) {
            return ((ArrowShooter)possessed).invokeGetArrow(((ProjectileEntityAccessor)firedArrow).invokeAsItemStack(), 1f);
        }
        return firedArrow;
    }
}
