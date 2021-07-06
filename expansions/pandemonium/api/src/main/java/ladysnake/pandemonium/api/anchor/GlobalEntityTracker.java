/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.pandemonium.api.anchor;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.ComponentPacketWriter;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * A {@link GlobalEntityTracker} tracks origins of ethereal players
 * having left their body.
 * <p>
 * Positions are saved to avoid having to load chunks and keep track
 * of every origin entity. Instead, such entities update their corresponding
 * tracked entry whenever their state changes.
 * <p>
 * The tracker is kept synchronized between server and clients.
 */
public interface GlobalEntityTracker extends CommonTickingComponent {
    ComponentKey<GlobalEntityTracker> KEY = ComponentRegistry.getOrCreate(new Identifier("pandemonium", "anchor_provider"), GlobalEntityTracker.class);

    static GlobalEntityTracker get(World world) {
        return KEY.get(world.getScoreboard());
    }

    /**
     *
     * @param anchorFactory the factory to use to create the anchor to track
     * @return the created anchor
     */
    FractureAnchor getOrCreate(FractureAnchorFactory anchorFactory);

    Optional<FractureAnchor> getAnchor(int anchorId);

    Optional<FractureAnchor> getAnchor(UUID anchorUuid);

    Collection<FractureAnchor> getAnchors();

    @Override
    void tick();

    void sync(ComponentPacketWriter writer);

    Optional<World> getWorld(RegistryKey<World> worldKey);
}
