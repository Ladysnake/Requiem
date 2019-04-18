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
package ladysnake.requiem.common.entity.ai;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class InertGoal extends Goal {
    private final Possessable owner;

    public InertGoal(Possessable owner) {
        super();
        this.owner = owner;
        this.setControls(EnumSet.allOf(Goal.Control.class));
    }

    @Override
    public boolean canStart() {
        return this.owner.isBeingPossessed();
    }

    @Override
    public boolean shouldContinue() {
        return this.owner.isBeingPossessed();
    }

    @Override
    public boolean canStop() {
        return false;
    }
}
