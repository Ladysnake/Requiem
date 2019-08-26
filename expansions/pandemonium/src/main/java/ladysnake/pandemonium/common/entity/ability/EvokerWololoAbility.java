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
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EvokerWololoAbility extends DirectAbilityBase<EvokerEntity> {
    private final EvokerEntity.WololoGoal wololoGoal;
    private boolean started;

    public EvokerWololoAbility(EvokerEntity owner) {
        super(owner);
        wololoGoal = owner.new WololoGoal();
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity entity) {
        boolean success = false;
        if (entity instanceof SheepEntity) {
            if (wololoGoal.canStart()) {
                wololoGoal.start();
                started = true;
                success = true;
            }
        }
        return success;
    }

    @Override
    public void update() {
        if (started) {
            if (wololoGoal.shouldContinue()) {
                wololoGoal.tick();
            } else {
                started = false;
                wololoGoal.stop();
            }
        }
    }

}
