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

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * A data type that can be stored in a {@link GlobalRecord}.
 *
 * @param <T> type for which the {@code RecordType} is being registered.
 * @since 2.0.0
 */
public final class RecordType<T> {
    @SuppressWarnings("unchecked")
    public static final SimpleRegistry<RecordType<?>> REGISTRY =
        FabricRegistryBuilder.createSimple((Class<RecordType<?>>) (Class<?>) RecordType.class, new Identifier("requiem", "record_types")).buildAndRegister();

    private final Identifier id;
    private final Codec<T> codec;
    private final BiPredicate<MinecraftServer, T> validityPredicate;

    private RecordType(Identifier id, Codec<T> codec, BiPredicate<MinecraftServer, T> validityPredicate) {
        this.id = id;
        this.codec = codec;
        this.validityPredicate = validityPredicate;
    }

    public <U> Optional<RecordType<U>> tryCast(Codec<U> witness) {
        if (this.codec == witness) {
            @SuppressWarnings("unchecked") RecordType<U> ret = (RecordType<U>) this;
            return Optional.of(ret);
        }
        return Optional.empty();
    }

    public Codec<T> getCodec() {
        return codec;
    }

    public boolean checkValidity(MinecraftServer context, T value) {
        return this.validityPredicate.test(context, value);
    }

    public Identifier getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    /**
     * Creates and registers a new {@link RecordType} with the given id.
     *
     * @param id    the unique identifier used to register the resulting record type
     * @param codec the {@link Codec} to use to (de)serialize associated data
     * @param <T>   type for which the {@code RecordType} is being registered
     * @return a newly registered {@link RecordType} for encoding instances of {@code T}
     */
    public static <T> RecordType<T> register(Identifier id, Codec<T> codec) {
        return register(id, codec, null);
    }

    /**
     * Creates and registers a new {@link RecordType} with the given id.
     *
     * @param id          the unique identifier used to register the resulting record type
     * @param codec       the {@link Codec} to use to (de)serialize associated data
     * @param worldGetter a getter for referenced worlds which absence could invalidate the record
     * @return a newly registered {@link RecordType} for encoding instances of {@code T}
     */
    public static <T> RecordType<T> register(Identifier id, Codec<T> codec, @Nullable Function<T, RegistryKey<World>> worldGetter) {
        return Registry.register(REGISTRY, id, new RecordType<>(
            id,
            codec,
            worldGetter == null ? (s, t) -> true : (s, t) -> s.getWorld(worldGetter.apply(t)) != null)
        );
    }

    @Deprecated(forRemoval = true)
    public static <T> RecordType<T> register(Identifier id, Codec<T> codec, @Nullable Function<T, RegistryKey<World>> worldGetter, @SuppressWarnings("unused") boolean required) {
        return register(id, codec, worldGetter);
    }
}
