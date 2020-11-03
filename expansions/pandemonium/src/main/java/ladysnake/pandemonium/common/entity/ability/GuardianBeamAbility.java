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
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class GuardianBeamAbility extends DirectAbilityBase<GuardianEntity> {
    private static final Constructor<? extends Goal> BEAM_GOAL_FACTORY;

    private final Goal fireBeamGoal;
    private boolean started;

    public GuardianBeamAbility(GuardianEntity owner) {
        super(owner);
        try {
            this.fireBeamGoal = BEAM_GOAL_FACTORY.newInstance(owner);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UncheckedReflectionException("Failed to instanciate FireBeamGoal", e);
        }
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) entity;
            owner.setTarget(target);
            if (fireBeamGoal.canStart()) {
                fireBeamGoal.start();
                started = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public void update() {
        if (started) {
            if (fireBeamGoal.shouldContinue()) {
                fireBeamGoal.tick();
            } else {
                started = false;
                fireBeamGoal.stop();
            }
        }
    }

    static {
        try {
            Class<? extends Goal> clazz = ReflectionHelper.findClass("net.minecraft.class_1577$class_1578");
            BEAM_GOAL_FACTORY = clazz.getDeclaredConstructor(GuardianEntity.class);
            BEAM_GOAL_FACTORY.setAccessible(true);
        } catch (ClassNotFoundException e) {
            throw new UncheckedReflectionException("Could not find the FireBeamGoal class", e);
        } catch (NoSuchMethodException e) {
            throw new UncheckedReflectionException("Could not find the FireBeamGoal constructor", e);
        }
    }
}
