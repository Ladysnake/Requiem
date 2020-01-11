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
package ladysnake.requiem.mixin.command;

import ladysnake.requiem.api.v1.internal.StatusEffectReapplicator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.command.EffectCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(EffectCommand.class)
public abstract class EffectCommandMixin {
    // ModifyVariable to capture the entity more easily
    @ModifyVariable(method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;clearStatusEffects()Z", shift = At.Shift.AFTER))
    private static Entity actuallyClearSoulboundEffects(Entity entity) {
        if (entity instanceof StatusEffectReapplicator) {
            ((StatusEffectReapplicator) entity).getReappliedStatusEffects().clear();
        }
        return entity;
    }

    // ModifyVariable to capture the entity more easily
    @ModifyVariable(method = "executeClear(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;Lnet/minecraft/entity/effect/StatusEffect;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tryRemoveStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", shift = At.Shift.AFTER))
    private static Entity actuallyClearSoulboundEffect(Entity entity, ServerCommandSource source, Collection<Entity> targets, StatusEffect effect) {
        if (entity instanceof StatusEffectReapplicator) {
            ((StatusEffectReapplicator) entity).getReappliedStatusEffects().removeIf(e -> e.getEffectType() == effect);
        }
        return entity;
    }
}
