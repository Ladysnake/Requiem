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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.GuardianEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class GuardianBeamAbility extends DirectAbilityBase<GuardianEntity, LivingEntity> {
    private final Goal fireBeamGoal;
    private boolean started;

    public GuardianBeamAbility(GuardianEntity owner) {
        super(owner, 15, LivingEntity.class, 0);
        this.fireBeamGoal = makeGoal(owner);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return target.isAlive();
    }

    @Override
    public boolean run(LivingEntity entity) {
        if (this.owner.world.isClient) return true;

        owner.setTarget(entity);
        if (fireBeamGoal.canStart()) {
            this.fireBeamGoal.start();
            this.beginCooldown();
            this.started = true;
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        if (started) {
            if (fireBeamGoal.shouldContinue()) {
                fireBeamGoal.tick();
            } else {
                started = false;
                fireBeamGoal.stop();
            }
        }
    }

    private static final Constructor<? extends Goal> BEAM_GOAL_FACTORY;

    private static Goal makeGoal(GuardianEntity owner) {
        try {
            return BEAM_GOAL_FACTORY.newInstance(owner);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UncheckedReflectionException("Failed to instantiate FireBeamGoal", e);
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
