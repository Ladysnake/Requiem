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
package ladysnake.requiem.mixin.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.minecraft.LivingEntityDropCallback;
import ladysnake.requiem.api.v1.remnant.MobResurrectable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract ItemStack getStackInHand(Hand hand_1);

    @Shadow public abstract Hand getActiveHand();

    @Shadow public abstract void setStackInHand(Hand hand_1, ItemStack itemStack_1);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "drop",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V"),
            cancellable = true
    )
    private void fireDropEvent(DamageSource deathCause, CallbackInfo ci) {
        if (LivingEntityDropCallback.EVENT.invoker().onEntityDrop((LivingEntity)(Object)this, deathCause)) {
            ci.cancel();
        } else if (this instanceof MobResurrectable) {
            ((MobResurrectable) this).spawnResurrectionEntity();
        }
    }

    /**
     * Fixes a bug in vanilla minecraft that gives back {@link ItemStack#finishUsing(World, LivingEntity)}'s result
     * even after an inventory drop
     */
    @Inject(method = "method_6040", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearActiveItem()V"))
    private void dropUsedItemAsSoul(CallbackInfo ci) {
        if (this instanceof RequiemPlayer && ((RequiemPlayer) this).asRemnant().isIncorporeal()&& !world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            this.dropStack(this.getStackInHand(this.getActiveHand()));
            this.setStackInHand(this.getActiveHand(), ItemStack.EMPTY);
        }
    }
}
