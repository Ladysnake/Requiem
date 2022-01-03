/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.mixin.common.possession.gameplay;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.VanillaRequiemPlugin;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends RangedWeaponItem {

    public CrossbowItemMixin(Settings settings) {
        super(settings);
    }

    @ModifyVariable(method = "loadProjectiles", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private static boolean giveCrossbowInfinity(boolean creative, LivingEntity shooter, ItemStack crossbow) {
        if (!creative) {
            MobEntity possessed = PossessionComponent.getHost(shooter);
            // the arrow consumption code is run on both sides for whatever reason. That complicates the random behaviour:
            // - if we tell the client to eat an arrow but the server does not, the server will not update back => desync
            // - if we tell the client to *not* eat an arrow but the server does, the server will update back => everything's fine
            // so we default to telling the client that we are never using arrows and letting the server do the work
            if (possessed instanceof CrossbowUser && (shooter.getRandom().nextFloat() < 0.5f || shooter.world.isClient)) {
                crossbow.getOrCreateNbt().putBoolean(VanillaRequiemPlugin.INFINITY_SHOT_TAG, true);
                return true;
            }
        }
        return creative;
    }

    @ModifyVariable(method = "shoot", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static boolean preventBoltPickup(boolean creative, World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative2, float speed, float divergence, float simulated) {
        if (!world.isClient && simulated == 0) {
            NbtCompound tag = crossbow.getNbt();
            if (tag != null && tag.getBoolean(VanillaRequiemPlugin.INFINITY_SHOT_TAG)) {
                tag.remove(VanillaRequiemPlugin.INFINITY_SHOT_TAG);
                return true;
            }
        }
        return creative;
    }
}
