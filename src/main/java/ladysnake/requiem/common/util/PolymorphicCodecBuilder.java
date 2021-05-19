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

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PolymorphicCodecBuilder<K, S> {
    private final Decoder<K> keyDecoder;
    private final Encoder<K> keyEncoder;
    private final Function<S, K> keyExtractor;
    private final Map<K, Codec<? extends S>> codecs;
    private final String keyName;

    private PolymorphicCodecBuilder(String keyName, Codec<K> keyCodec, Function<S, K> keyExtractor) {
        this.keyName = keyName;
        this.keyDecoder = keyCodec.fieldOf(keyName).decoder();
        this.keyEncoder = keyCodec;
        this.keyExtractor = keyExtractor;
        this.codecs = new HashMap<>();
    }

    public static <K, S> PolymorphicCodecBuilder<K, S> create(String keyName, Codec<K> keyElementCodec, Function<S, K> keyExtractor) {
        return new PolymorphicCodecBuilder<>(keyName, keyElementCodec, keyExtractor);
    }

    public PolymorphicCodecBuilder<K, S> with(K key, Codec<? extends S> codec) {
        this.codecs.put(key, codec);
        return this;
    }

    public Codec<S> build() {
        return Codec.PASSTHROUGH.flatXmap(
            d -> keyDecoder.parse(d).flatMap(key -> {
                Codec<? extends S> c = this.codecs.get(key);
                if (c == null) return DataResult.error(String.format("Invalid/Unsupported value '%s' was found for key '%s'", key, keyName));
                return c.parse(d);
            }),
            d -> {
                K key = keyExtractor.apply(d);
                // This is the codec for this specific object type, it *should* be fine
                @SuppressWarnings("unchecked") Codec<S> c = (Codec<S>) this.codecs.get(key);
                if (c == null) return DataResult.error(String.format("Invalid/Unsupported value \"%s\" was found for key '%s'", key, keyName));
                return c.encodeStart(JsonOps.INSTANCE, d).flatMap(j -> keyEncoder.encodeStart(JsonOps.INSTANCE, key).map(k -> {
                    JsonObject json = j.getAsJsonObject();
                    json.add(keyName, k);
                    return new Dynamic<>(JsonOps.INSTANCE, j);
                }));
            }
        );
    }
}
