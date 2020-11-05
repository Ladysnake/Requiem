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

import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class RangedAttackAbility<T extends MobEntity & RangedAttackMob> implements DirectAbility<T> {

    private final T owner;
    private final double range = 64.0;
    private final int intervalTicks = 40;
    private int cooldown = 0;

    public RangedAttackAbility(T owner) {
        this.owner = owner;
    }

    @Override
    public double getRange() {
        return range;
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity target) {
        if (this.cooldown == 0 && target instanceof LivingEntity) {
            this.cooldown = this.intervalTicks;

            if (!player.world.isClient) {
                this.owner.attack((LivingEntity) target, 1f);
            }

            return true;
        }
        return false;
    }

    @Override
    public void update() {
        if (this.cooldown > 0) {
            this.cooldown--;
        }
    }
}
