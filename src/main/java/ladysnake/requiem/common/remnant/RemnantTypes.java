/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.common.remnant;

import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.core.remnant.NullRemnantState;
import net.minecraft.util.Identifier;

public final class RemnantTypes {
    private RemnantTypes() { throw new AssertionError(); }

    public static final RemnantType MORTAL = new SimpleRemnantType(p -> NullRemnantState.INSTANCE, false, "requiem:opus.mortal_sentence", () -> RequiemItems.PURE_SOUL_VESSEL);
    public static final RemnantType REMNANT = new SimpleRemnantType(DemonRemnantState::new, true, "requiem:opus.remnant_sentence", () -> RequiemItems.SEALED_REMNANT_VESSEL);

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
