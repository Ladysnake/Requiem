/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;

/**
 * A {@link DirectAbility} targets a specific entity
 *
 * @param <E> The type of mobs that can wield this ability
 */
public abstract class DirectAbilityBase<E extends MobEntity, T extends Entity> extends AbilityBase<E> implements DirectAbility<E, T> {
    private final double range;
    private final Class<T> targetType;

    protected DirectAbilityBase(E owner, double range, Class<T> targetType, int cooldown) {
        super(owner, cooldown);
        this.range = range;
        this.targetType = targetType;
    }

    @Override
    public Class<T> getTargetType() {
        return targetType;
    }

    @Override
    public double getRange() {
        return range;
    }

    /**
     * Triggers the ability on a known entity.
     *
     * @param target the targeted entity
     * @return <code>true</code> if the ability has been successfully used
     */
    @Override
    public boolean trigger(T target) {
        if (this.getCooldown() == 0 && this.canTarget(target)) {
            return this.run(target);
        }
        return false;
    }

    protected abstract boolean run(T target);
}
