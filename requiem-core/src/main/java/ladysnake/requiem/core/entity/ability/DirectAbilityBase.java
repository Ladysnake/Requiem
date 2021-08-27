/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.core.entity.ability;

import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/**
 * A {@link DirectAbility} targets a specific entity
 *
 * @param <E> The type of mobs that can wield this ability
 */
public abstract class DirectAbilityBase<E extends LivingEntity, T extends Entity> extends AbilityBase<E> implements DirectAbility<E, T> {

    private final double range;
    private final Class<T> targetType;

    protected DirectAbilityBase(E owner, int cooldown, double range, Class<T> targetType) {
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

    @Override
    public boolean canTarget(T target) {
        // Stop hurting yourself
        return target != this.owner;
    }

    /**
     * Triggers the ability on a known entity.
     *
     * @param target the targeted entity
     * @return <code>true</code> if the ability has been successfully used
     */
    @Override
    public ActionResult trigger(T target) {
        if (this.getCooldown() == 0 && this.canTarget(target)) {
            return this.run(target) ? ActionResult.SUCCESS : ActionResult.FAIL;
        }
        return ActionResult.FAIL;
    }

    protected abstract boolean run(T target);
}
