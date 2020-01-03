/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.impl.remnant.MutableRemnantState;
import ladysnake.requiem.common.impl.remnant.NullRemnantState;
import ladysnake.requiem.common.item.RequiemItems;
import net.minecraft.util.Identifier;

public final class RemnantTypes {
    private RemnantTypes() { throw new AssertionError(); }

    public static final RemnantType MORTAL = new SimpleRemnantType(p -> NullRemnantState.NULL_STATE, false, "requiem:opus.mortal_sentence", () -> RequiemItems.OPUS_DEMONIUM_CURE);
    public static final RemnantType REMNANT = new SimpleRemnantType(owner -> new MutableRemnantState(RemnantTypes.REMNANT, owner), true, "requiem:opus.remnant_sentence", () -> RequiemItems.OPUS_DEMONIUM_CURSE);

    public static RemnantType get(Identifier id) {
        return RequiemRegistries.REMNANT_STATES.get(id);
    }

    public static RemnantType get(int rawId) {
        return RequiemRegistries.REMNANT_STATES.get(rawId);
    }

    public static Identifier getId(RemnantType type) {
        return RequiemRegistries.REMNANT_STATES.getId(type);
    }

    public static int getRawId(RemnantType type) {
        return RequiemRegistries.REMNANT_STATES.getRawId(type);
    }

}
