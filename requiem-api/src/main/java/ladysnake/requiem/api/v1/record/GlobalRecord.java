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
package ladysnake.requiem.api.v1.record;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A {@link GlobalRecord} represents a serializable collection of data accessible from anywhere in the world.
 * Global records are tracked regardless of distance and loaded chunks.
 *
 * @since 2.0.0
 */
public interface GlobalRecord {
    /**
     * Returns the constant identifier that uniquely identifies the record
     * within its {@link GlobalRecordKeeper}. This ID will not change whenever the record is
     * loaded from disk and may not be reused.
     *
     * @return the constant UUID of the anchor
     */
    UUID getUuid();

    <T> Optional<T> get(RecordType<T> type);

    <T> void put(RecordType<T> type, @Nullable T data);

    void remove(RecordType<?> type);

    /**
     * Shorthand for {@code get(type).isPresent()}
     */
    <T> boolean has(RecordType<T> type);

    boolean isValid();

    void invalidate();

    Stream<RecordType<?>> types();

    NbtCompound toTag();

    @Deprecated(forRemoval = true)
    default int getId() {
        throw new UnsupportedOperationException();
    }

    @Deprecated(forRemoval = true)
    default void addTickingAction(Identifier actionId, Consumer<GlobalRecord> action) {
        throw new UnsupportedOperationException();
    }

    @Deprecated(forRemoval = true)
    default void removeTickingAction(Identifier actionId) {
        throw new UnsupportedOperationException();
    }
}
