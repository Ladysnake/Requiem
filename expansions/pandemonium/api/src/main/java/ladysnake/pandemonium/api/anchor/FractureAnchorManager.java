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
package ladysnake.pandemonium.api.anchor;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
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
public interface FractureAnchorManager extends ComponentV3 {
    ComponentKey<FractureAnchorManager> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("pandemonium", "anchor_provider"), FractureAnchorManager.class);

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
