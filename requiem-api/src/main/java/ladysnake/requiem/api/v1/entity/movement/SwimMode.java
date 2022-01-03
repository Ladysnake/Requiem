/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.api.v1.entity.movement;

import ladysnake.requiem.api.v1.entity.MovementAlterer;
import net.fabricmc.fabric.api.util.TriState;

public enum SwimMode {
    /**
     * The entity cannot swim but can still freely move in water
     */
    DISABLED(TriState.FALSE),
    /**
     * The entity can start swimming, and they can stop swimming when they want to
     */
    ENABLED(TriState.DEFAULT),
    /**
     * The entity is always swimming, like a fish
     */
    FORCED(TriState.TRUE),
    /**
     * The entity is always sinking to the bottom
     */
    SINKING(TriState.FALSE),
    /**
     * The entity is always floating at the surface of the water
     */
    FLOATING(TriState.FALSE),
    /**
     * No information, the {@link MovementAlterer} should use heuristics to determine which of the
     * other modes to use
     */
    UNSPECIFIED(TriState.DEFAULT);

    private final TriState sprintSwim;

    SwimMode(TriState allowSprintSwim) {
        this.sprintSwim = allowSprintSwim;
    }

    public TriState sprintSwims() {
        return sprintSwim;
    }
}
