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
package ladysnake.requiem.common.advancement.criterion;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import javax.annotation.Nullable;

public abstract class RemnantTypePredicate {
    public static final RemnantTypePredicate ANY = new RemnantTypePredicate() {
        @Override
        public boolean matches(RemnantType type) {
            return true;
        }

        @Override
        public JsonElement serialize() {
            return JsonNull.INSTANCE;
        }
    };
    private static final Joiner COMMA_JOINER = Joiner.on(", ");

    public abstract boolean matches(RemnantType var1);

    public abstract JsonElement serialize();

    public static RemnantTypePredicate deserialize(@Nullable JsonElement json) {
        if (json != null && !json.isJsonNull()) {
            Identifier id = new Identifier(JsonHelper.asString(json, "type"));
            RemnantType type = RequiemRegistries.REMNANT_STATES.getOrEmpty(id).orElseThrow(() -> new JsonSyntaxException("Unknown remnant type '" + id + "', valid types are: " + COMMA_JOINER.join(RequiemRegistries.REMNANT_STATES.getIds())));
            return new Single(type);
        } else {
            return ANY;
        }
    }
    static class Single extends RemnantTypePredicate {
        private final RemnantType type;

        public Single(RemnantType type) {
            this.type = type;
        }

        @Override
        public boolean matches(RemnantType type) {
            return this.type == type;
        }

        @Override
        public JsonElement serialize() {
            return new JsonPrimitive(RemnantTypes.getId(this.type).toString());
        }
    }

}
