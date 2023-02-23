/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A {@link GlobalRecordKeeper} stores recorded data for access from anywhere
 * in the world.
 *
 * <p>Positions are saved to avoid having to load chunks and keep track
 * of every origin entity. Instead, such entities update their corresponding
 * tracked entry whenever their state changes.
 *
 * <p><strike>The tracker is kept synchronized between server and clients.</strike>
 * (To be reimplemented if needed)
 */
public interface GlobalRecordKeeper extends CommonTickingComponent {
    ComponentKey<GlobalRecordKeeper> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "global_record_keeper"), GlobalRecordKeeper.class);

    static GlobalRecordKeeper get(World world) {
        return KEY.get(world.getScoreboard());
    }

    static GlobalRecordKeeper get(MinecraftServer server) {
        return KEY.get(server.getScoreboard());
    }

    GlobalRecord createRecord();

    @Deprecated(forRemoval = true)
    default Optional<GlobalRecord> getRecord(int anchorId) {
        throw new UnsupportedOperationException();
    }

    Optional<GlobalRecord> getRecord(UUID anchorUuid);

    Collection<GlobalRecord> getRecords();

    Stream<GlobalRecord> stream();

    @Override
    void tick();

    @Deprecated(forRemoval = true)
    default Optional<World> getWorld(RegistryKey<World> worldKey) {
        throw new UnsupportedOperationException();
    }
}
