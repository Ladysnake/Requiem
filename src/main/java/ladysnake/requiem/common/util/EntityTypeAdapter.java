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
package ladysnake.requiem.common.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.EntityType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

public final class EntityTypeAdapter extends TypeAdapter<EntityType<?>> {
    @Override
    public void write(JsonWriter out, EntityType<?> value) throws IOException {
        out.value(Objects.requireNonNull(EntityType.getId(value)).toString());
    }

    @Nullable
    @Override
    public EntityType<?> read(JsonReader in) throws IOException {
        return EntityType.get(in.nextString()).orElse(null);
    }
}
