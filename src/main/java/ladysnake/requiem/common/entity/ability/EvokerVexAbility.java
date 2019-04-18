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
package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.common.util.reflection.ReflectionHelper;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.invoke.MethodType;
import java.util.function.Function;

import static ladysnake.requiem.common.util.reflection.ReflectionHelper.pick;

public class EvokerVexAbility extends IndirectAbilityBase<EvokerEntity> {
    private static final Function<EvokerEntity, ? extends SpellcastingIllagerEntity.CastSpellGoal> VEX_GOAL_FACTORY;

    private final SpellcastingIllagerEntity.CastSpellGoal summonVexGoal;
    private boolean started;

    public EvokerVexAbility(EvokerEntity owner) {
        super(owner);
        summonVexGoal = VEX_GOAL_FACTORY.apply(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player) {
        boolean success = false;
        owner.setTarget(owner); // The target needs to be non null to let the goal run
        if (summonVexGoal.canStart()) {
            summonVexGoal.start();
            started = true;
            success = true;
        }
        owner.setTarget(null);
        return success;
    }

    @Override
    public void update() {
        if (started) {
            owner.setTarget(owner);
            if (summonVexGoal.shouldContinue()) {
                summonVexGoal.tick();
            } else {
                owner.setSpell(SpellcastingIllagerEntity.class_1618.NONE);
                started = false;
            }
            owner.setTarget(null);
        }
    }

    static {
        try {
            Class<?> clazz = Class.forName(pick("net.minecraft.class_1564$class_1567", "net.minecraft.entity.mob.EvokerEntity$SummonVexGoal"));
            VEX_GOAL_FACTORY = ReflectionHelper.createFactory(
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
