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

import ladysnake.pandemonium.mixin.common.entity.mob.EvokerEntityAccessor;
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
    public double getRange() {
        return 16;
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity entity) {
        if (entity instanceof SheepEntity) {
            if (player.world.isClient) return true;

            if (this.wololoGoal.canStart()) {
                this.wololoGoal.start();
                ((EvokerEntityAccessor) this.owner).invokeSetWololoTarget((SheepEntity) entity);
                this.started = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public void update() {
        if (this.started) {
            if (this.wololoGoal.shouldContinue()) {
                this.wololoGoal.tick();
            } else {
                this.started = false;
                this.wololoGoal.stop();
            }
        }
    }
}
