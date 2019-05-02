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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin extends Item {
    private static ThreadLocal<Boolean> REVERT_CREATIVE_MODE = ThreadLocal.withInitial(() -> false);

    public TridentItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onItemStopUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/TridentEntity;method_7474(Lnet/minecraft/entity/Entity;FFFFF)V"))
    private void giveDrownedInfinity(ItemStack stack, World world, LivingEntity user, int ticks, CallbackInfo ci) {
        if (((RequiemPlayer)user).getPossessionComponent().getPossessedEntity() instanceof DrownedEntity && random.nextFloat() < 0.8f) {
            PlayerAbilities abilities = ((PlayerEntity) user).abilities;
            if (!abilities.creativeMode) {
                // Makes the trident not consume the item
                abilities.creativeMode = true;
                REVERT_CREATIVE_MODE.set(true);
            }
        }
    }

    @Inject(method = "onItemStopUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/stat/Stat;)V"))
    private void revertCreativeMode(ItemStack stack, World world, LivingEntity user, int ticks, CallbackInfo ci) {
        if (REVERT_CREATIVE_MODE.get()) {
            ((PlayerEntity)user).abilities.creativeMode = false;
            REVERT_CREATIVE_MODE.set(false);
        }
    }
}
