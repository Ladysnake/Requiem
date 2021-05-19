/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.common.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.Text;

import java.util.Locale;

public final class MoreCodecs {
    private static final Gson GSON = new Gson();
    public static final Codec<JsonElement> STRING_JSON = Codec.STRING.xmap(
        str -> GSON.fromJson(str, JsonElement.class),
        GSON::toJson
    );

    public static final Codec<JsonElement> DYNAMIC_JSON = Codec.PASSTHROUGH.comapFlatMap(
        dynamic -> DataResult.success(dynamic.convert(JsonOps.INSTANCE).getValue()),
        json -> new Dynamic<>(JsonOps.INSTANCE, json)
    );

    public static Codec<Text> text(Codec<JsonElement> jsonCodec) {
        return jsonCodec.xmap(Text.Serializer::fromJson, Text.Serializer::toJsonTree);
    }

    public static <E extends Enum<E>> Codec<E> enumeration(Class<E> enumType) {
        return Codec.STRING.xmap(s -> Enum.valueOf(enumType, s.toUpperCase(Locale.ROOT)), Enum::name);
    }
}
