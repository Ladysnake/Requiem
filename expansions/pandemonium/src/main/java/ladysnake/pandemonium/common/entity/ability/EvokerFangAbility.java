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
package ladysnake.pandemonium.common.entity.ability;

import ladysnake.requiem.common.entity.ability.DirectAbilityBase;
import ladysnake.requiem.common.util.reflection.ReflectionHelper;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import static ladysnake.requiem.common.util.reflection.ReflectionHelper.pick;

public class EvokerFangAbility extends DirectAbilityBase<EvokerEntity> {
    private static final Function<EvokerEntity, ? extends SpellcastingIllagerEntity.CastSpellGoal> FANGS_GOAL_FACTORY;
    private static final MethodHandle CAST_SPELL_GOAL$CAST_SPELL;

    static {
        try {
            CAST_SPELL_GOAL$CAST_SPELL = MethodHandles.lookup().findVirtual(SpellcastingIllagerEntity.CastSpellGoal.class, pick("method_7148", "castSpell"), MethodType.methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new UncheckedReflectionException(e);
        }
    }

    private final SpellcastingIllagerEntity.CastSpellGoal conjureFangsGoal;

    public EvokerFangAbility(EvokerEntity owner) {
        super(owner);
        this.conjureFangsGoal = FANGS_GOAL_FACTORY.apply(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity entity) {
        boolean success = false;
        if (entity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) entity;
            owner.setTarget(target);
            if (conjureFangsGoal.canStart()) {
                try {
                    CAST_SPELL_GOAL$CAST_SPELL.invokeExact(conjureFangsGoal);
                } catch (Throwable throwable) {
                    throw new UncheckedReflectionException("Failed to trigger evoker fang ability", throwable);
                }
                success = true;
            }
            owner.setTarget(null);
        }
        return success;
    }

    static {
        try {
            Class<?> clazz = Class.forName(pick("net.minecraft.class_1564$class_1565", "net.minecraft.entity.mob.EvokerEntity$ConjureFangsGoal"));
            FANGS_GOAL_FACTORY = ReflectionHelper.createFactory(
                    clazz,
                    "apply",
                    Function.class,
                    ReflectionHelper.getTrustedLookup(clazz),
                    MethodType.methodType(Object.class, Object.class),
                    EvokerEntity.class
            );
        } catch (ClassNotFoundException e) {
            throw new UncheckedReflectionException("Could not find the ConjureFangsGoal class", e);
        }
    }
}
