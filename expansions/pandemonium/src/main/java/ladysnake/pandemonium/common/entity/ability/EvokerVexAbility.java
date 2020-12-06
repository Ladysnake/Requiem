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

import ladysnake.requiem.common.entity.ability.IndirectAbilityBase;
import ladysnake.requiem.common.util.reflection.ReflectionHelper;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class EvokerVexAbility extends IndirectAbilityBase<EvokerEntity> {
    private static final Constructor<? extends SpellcastingIllagerEntity.CastSpellGoal> VEX_GOAL_FACTORY;

    private final SpellcastingIllagerEntity.CastSpellGoal summonVexGoal;
    private boolean started;

    public EvokerVexAbility(EvokerEntity owner) {
        super(owner, 0);
        try {
            summonVexGoal = VEX_GOAL_FACTORY.newInstance(owner);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UncheckedReflectionException("Failed to instanciate SummonVexGoal", e);
        }
    }

    @Override
    public boolean run() {
        if (this.owner.world.isClient) return true;

        boolean hasTarget = owner.getTarget() != null;
        if (!hasTarget) owner.setTarget(owner); // Need to have some kind of target to cast the spell

        try {
            if (this.summonVexGoal.canStart()) {
                this.summonVexGoal.start();
                this.started = true;
                this.beginCooldown();
                return true;
            }
        } finally {
            if (!hasTarget) owner.setTarget(null);
        }

        return false;
    }

    @Override
    public void update() {
        super.update();
        if (started) {
            boolean hasTarget = owner.getTarget() != null;
            if (!hasTarget) owner.setTarget(owner); // Need to have some kind of target to cast the spell

            try {
                if (summonVexGoal.shouldContinue()) {
                    summonVexGoal.tick();
                } else {
                    started = false;
                    this.summonVexGoal.stop();
                    owner.setSpell(SpellcastingIllagerEntity.Spell.NONE);
                }
            } finally {
                if (!hasTarget) owner.setTarget(null);
            }
        }
    }

    static {
        try {
            Class<? extends SpellcastingIllagerEntity.CastSpellGoal> clazz = ReflectionHelper.findClass("net.minecraft.class_1564$class_1567");
            VEX_GOAL_FACTORY = clazz.getDeclaredConstructor(EvokerEntity.class);
            VEX_GOAL_FACTORY.setAccessible(true);
        } catch (ClassNotFoundException e) {
            throw new UncheckedReflectionException("Could not find the SummonVexGoal class", e);
        } catch (NoSuchMethodException e) {
            throw new UncheckedReflectionException("Could not get the SummonVexGoal constructor", e);
        }
    }
}
