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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.MobEntity;

public class RangedAttackAbility<T extends MobEntity & RangedAttackMob> extends DirectAbilityBase<T, LivingEntity> {

    public RangedAttackAbility(T owner) {
        super(owner, 20.0, LivingEntity.class, 40);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return true;
    }

    @Override
    public boolean run(LivingEntity target) {
        if (!this.owner.world.isClient) {
            this.owner.attack(target, 1f);
        }

        return true;
    }
}
