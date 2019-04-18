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
package ladysnake.requiem.common.remnant;

import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.impl.remnant.NullRemnantState;

public final class RemnantStates {
    private RemnantStates() { throw new AssertionError(); }

    public static final RemnantType MORTAL = p -> NullRemnantState.NULL_STATE;
    public static final RemnantType LARVA = owner -> new FracturableRemnantState(RemnantStates.LARVA, owner);
    public static final RemnantType YOUNG = owner -> new AstralRemnantState(RemnantStates.YOUNG, owner);
}
