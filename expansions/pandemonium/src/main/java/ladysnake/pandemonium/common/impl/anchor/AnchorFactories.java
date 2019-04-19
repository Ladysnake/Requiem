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
package ladysnake.pandemonium.common.impl.anchor;

import ladysnake.pandemonium.api.anchor.FractureAnchorFactory;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.UUID;

public class AnchorFactories {
    public static FractureAnchorFactory fromEntityUuid(UUID entityUuid) {
        return (manager, uuid, id) -> new EntityFractureAnchor(entityUuid, manager, uuid, id);
    }

    @Nonnull
    public static FractureAnchorFactory fromTag(CompoundTag anchorTag) {
        if (anchorTag.getString("AnchorType").equals("requiem:entity")) {
            return entityAnchorFromTag(anchorTag);
        }
        return trackedAnchorFromTag(anchorTag);
    }

    private static FractureAnchorFactory entityAnchorFromTag(CompoundTag serializedAnchor) {
        return (manager, uuid, id) -> new EntityFractureAnchor(manager, serializedAnchor, id);
    }

    private static FractureAnchorFactory trackedAnchorFromTag(CompoundTag serializedAnchor) {
        return (manager, uuid, id) -> new TrackedFractureAnchor(manager, serializedAnchor, id);
    }
}
