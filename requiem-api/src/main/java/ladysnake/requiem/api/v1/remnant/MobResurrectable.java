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
package ladysnake.requiem.api.v1.remnant;

import net.minecraft.entity.mob.MobEntity;

/**
 * An entity implementing {@link MobResurrectable} can be resurrected as a new mob after death.
 * All {@link net.minecraft.server.network.ServerPlayerEntity} implement this interface.
 */
public interface MobResurrectable {
    /**
     * Sets the mob that this player will use as its second life.
     * <p>
     * This should not be called with an entity that is already spawned in the world.
     * If the player is remnant, the resurrection entity will be spawned
     * when the player respawns, otherwise it will spawn when the entity's inventory is dropped.
     */
    void setResurrectionEntity(MobEntity secondLife);

    boolean hasResurrectionEntity();

    /**
     * Spawns a previously set resurrection entity, and make the player start possessing the entity.
     */
    void spawnResurrectionEntity();
}
