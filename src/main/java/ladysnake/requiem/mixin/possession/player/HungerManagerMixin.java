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
package ladysnake.requiem.mixin.possession.player;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {
    @Unique
    private static final ThreadLocal<PlayerEntity> PLAYER_ENTITY_THREAD_LOCAL = new ThreadLocal<>();

    @Shadow
    private float exhaustion;

    @Shadow
    private int foodLevel;

    @Inject(method = "update", at = @At(value = "INVOKE", ordinal = 0))
    private void updateSoulHunger(PlayerEntity playerEntity, CallbackInfo ci) {
        RequiemPlayer requiemPlayer = RequiemPlayer.from(playerEntity);
        if (requiemPlayer.asRemnant().isSoul()) {
            Possessable possessed = (Possessable) requiemPlayer.asPossessor().getPossessedEntity();
            if (possessed == null || !possessed.isRegularEater()) {
                this.exhaustion = 0;
                this.foodLevel = 20;
            }
        }
        PLAYER_ENTITY_THREAD_LOCAL.set(playerEntity);
    }

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V"))
    private float healPossessedEntity(float amount) {
        LivingEntity possessedEntity = RequiemPlayer.from(PLAYER_ENTITY_THREAD_LOCAL.get()).asPossessor().getPossessedEntity();
        if (possessedEntity != null && ((Possessable) possessedEntity).isRegularEater()) {
            possessedEntity.heal(amount);
        }
        return amount;
    }

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private float damagePossessedEntity(float amount) {
        LivingEntity possessedEntity = RequiemPlayer.from(PLAYER_ENTITY_THREAD_LOCAL.get()).asPossessor().getPossessedEntity();
        if (possessedEntity != null && ((Possessable) possessedEntity).isRegularEater()) {
            possessedEntity.damage(DamageSource.STARVE, amount);
        }
        return amount;
    }
}
