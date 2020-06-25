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

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * A {@link FractureAnchor} represents the origin of an ethereal player
 * who left their body.
 * Anchors are tracked regardless of distance and loaded chunks.
 */
public interface FractureAnchor {
    /**
     * Returns the constant shorter ID of the fracture anchor that uniquely identifies the fracture anchor
     * within its {@link FractureAnchorManager}. This ID may change whenever the fracture anchor is
     * loaded from disk and may be reused.
     *
     * @return the constant short ID of the anchor
     */
    int getId();

    /**
     * Returns the constant longer UUID of the fracture anchor that uniquely identifies the fracture anchor
     * within its {@link FractureAnchorManager}. This ID will not change whenever the fracture anchor is
     * loaded from disk and may not be reused.
     *
     * @return the constant UUID of the anchor
     */
    UUID getUuid();

    double getX();

    double getY();

    double getZ();

    void setPosition(double x, double y, double z);

    void update();

    boolean isInvalid();

    void invalidate();

    CompoundTag toTag(CompoundTag tag);
}
