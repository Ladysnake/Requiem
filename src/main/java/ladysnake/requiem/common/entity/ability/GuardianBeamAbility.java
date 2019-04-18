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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.lang.invoke.MethodType;
import java.util.function.Function;

import static ladysnake.requiem.common.util.reflection.ReflectionHelper.pick;

public class GuardianBeamAbility extends DirectAbilityBase<GuardianEntity> {
    private static final Function<GuardianEntity, ? extends Goal> BEAM_GOAL_FACTORY;

    private final Goal fireBeamGoal;
    private boolean started;

    public GuardianBeamAbility(GuardianEntity owner) {
        super(owner);
        this.fireBeamGoal = BEAM_GOAL_FACTORY.apply(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity entity) {
        boolean success = false;
        if (entity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) entity;
            owner.setTarget(target);
            if (fireBeamGoal.canStart()) {
                fireBeamGoal.start();
                success = true;
            }
        }
        return success;
    }

    @Override
    public void update() {
        if (started && fireBeamGoal.shouldContinue()) {
            fireBeamGoal.tick();
        } else {
            started = false;
        }
    }

    static {
        try {
            Class<?> clazz = Class.forName(pick("net.minecraft.class_1577$class_1578", "net.minecraft.entity.mob.GuardianEntity$FireBeamGoal"));
            BEAM_GOAL_FACTORY = ReflectionHelper.createFactory(
                    clazz,
                    "apply",
                    Function.class,
                    ReflectionHelper.getTrustedLookup(clazz),
                    MethodType.methodType(Object.class, Object.class),
                    GuardianEntity.class
            );
        } catch (ClassNotFoundException e) {
            throw new UncheckedReflectionException("Could not find the ConjureFangsGoal class", e);
        }
    }
}
