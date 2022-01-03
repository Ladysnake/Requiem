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
package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.core.entity.ability.TickingGoalAbility;
import ladysnake.requiem.core.util.reflection.ReflectionHelper;
import ladysnake.requiem.core.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.GuardianEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class GuardianBeamAbility extends TickingGoalAbility<GuardianEntity, LivingEntity> {
    public GuardianBeamAbility(GuardianEntity owner) {
        super(owner, makeGoal(owner), 20*4, 15, LivingEntity.class);
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
