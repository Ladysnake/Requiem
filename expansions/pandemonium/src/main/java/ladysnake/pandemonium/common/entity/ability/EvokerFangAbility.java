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
import ladysnake.requiem.common.util.reflection.UnableToFindMethodException;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EvokerFangAbility extends DirectAbilityBase<EvokerEntity, LivingEntity> {
    private static final Constructor<? extends SpellcastingIllagerEntity.CastSpellGoal> FANGS_GOAL_FACTORY;
    private static final Method CAST_SPELL_GOAL$CAST_SPELL;

    private final SpellcastingIllagerEntity.CastSpellGoal conjureFangsGoal;
    private int cooldown = 0;

    public EvokerFangAbility(EvokerEntity owner) {
        super(owner, 12, LivingEntity.class);
        try {
            this.conjureFangsGoal = FANGS_GOAL_FACTORY.newInstance(owner);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new UncheckedReflectionException(e);
        }
    }

    @Override
    public boolean canTrigger(LivingEntity target) {
        return target.isAlive();
    }

    @Override
    public boolean run(LivingEntity entity) {
        if (this.owner.world.isClient) return true;

        this.owner.setTarget(entity);

        boolean success;

        if (this.conjureFangsGoal.canStart()) {
            try {
                CAST_SPELL_GOAL$CAST_SPELL.invoke(conjureFangsGoal);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new UncheckedReflectionException("Failed to trigger evoker fang ability", e);
            }
            this.owner.setSpell(SpellcastingIllagerEntity.Spell.FANGS);
            this.cooldown = 40;
            success = true;
        } else {
            success = false;
        }
        owner.setTarget(null);
        return success;
    }

    @Override
    public void update() {
        if (cooldown > 0) {
            --cooldown;
            if (cooldown == 0 && !this.owner.world.isClient) {
                this.owner.setSpell(SpellcastingIllagerEntity.Spell.NONE);
            }
        }
    }

    static {
        try {
            CAST_SPELL_GOAL$CAST_SPELL = ReflectionHelper.findMethodFromIntermediary(SpellcastingIllagerEntity.CastSpellGoal.class, "method_7148", void.class);
            Class<? extends SpellcastingIllagerEntity.CastSpellGoal> clazz = ReflectionHelper.findClass("net.minecraft.class_1564$class_1565");
            FANGS_GOAL_FACTORY = clazz.getDeclaredConstructor(EvokerEntity.class);
            FANGS_GOAL_FACTORY.setAccessible(true);
        } catch (ClassNotFoundException e) {
            throw new UncheckedReflectionException("Could not find the ConjureFangsGoal class", e);
        } catch (UnableToFindMethodException e) {
            throw new UncheckedReflectionException("Could not find the castSpell method", e);
        } catch (NoSuchMethodException e) {
            throw new UncheckedReflectionException("Could not find the ConjureFangsGoal constructor", e);
        }
    }
}
