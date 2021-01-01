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

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

/**
 * A {@link FractureAnchorManager} tracks origins of ethereal players
 * having left their body.
 * <p>
 * Positions are saved to avoid having to load chunks and keep track
 * of every origin entity. Instead, such entities update their corresponding
 * tracked entry whenever their state changes.
 * <p>
 * The tracker is kept synchronized between server and clients.
 */
public interface FractureAnchorManager extends Component {
    ComponentKey<FractureAnchorManager> KEY = ComponentRegistry.getOrCreate(new Identifier("pandemonium", "anchor_provider"), FractureAnchorManager.class);

    static FractureAnchorManager get(World world) {
        return KEY.get(world);
    }

    /**
     *
     * @param anchorFactory the factory to use to create the anchor to track
     * @return the created anchor
     */
    FractureAnchor addAnchor(FractureAnchorFactory anchorFactory);

    @Nullable FractureAnchor getAnchor(int anchorId);

    @Nullable FractureAnchor getAnchor(UUID anchorUuid);

    Collection<FractureAnchor> getAnchors();

    void updateAnchors(long time);

    World getWorld();
}
