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
package ladysnake.requiem.api.v1.entity;

import ladysnake.requiem.api.v1.internal.ApiInternals;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Registry for {@link MovementConfig}.
 *
 * <p> This registry is data-driven; entries are populated through
 * <tt>requiem/entity_mobility.json</tt> data files.
 */
public interface MovementRegistry {
    /**
     * Retrieve the movement registry for the given {@link World}.
     *
     * <p> If {@code world} is {@code null}, the returned dialogue registry is the one
     * used by server worlds.
     *
     * @param world the world for which to get the movement registry, or {@code null} to get the main registry
     * @return the movement registry for the given world
     */
    static MovementRegistry get(@Nullable World world) {
        return ApiInternals.getMovementRegistry(world);
    }

    MovementConfig getEntityMovementConfig(EntityType<?> type);
}
