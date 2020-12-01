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

    private final int intervalTicks;
    private int cooldown = 0;

    public RangedAttackAbility(T owner) {
        super(owner, 20.0, LivingEntity.class);
        intervalTicks = 40;
    }

    @Override
    public boolean canTrigger(LivingEntity target) {
        return this.cooldown == 0;
    }

    @Override
    public boolean run(LivingEntity target) {
        this.cooldown = this.intervalTicks;

        if (!this.owner.world.isClient) {
            this.owner.attack(target, 1f);
        }

        return true;
    }

    @Override
    public void update() {
        if (this.cooldown > 0) {
            this.cooldown--;
        }
    }
}
