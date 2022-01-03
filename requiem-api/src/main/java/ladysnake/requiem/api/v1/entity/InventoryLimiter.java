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

import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.InventoryNode;
import ladysnake.requiem.api.v1.internal.ApiInternals;
import net.minecraft.entity.player.PlayerEntity;

public interface InventoryLimiter {
    static InventoryLimiter instance() {
        return ApiInternals.getInventoryLimiter();
    }

    InventoryLock getLock();

    void enable(PlayerEntity player);
    void disable(PlayerEntity player);

    void unlock(PlayerEntity player, InventoryNode part);
    void lock(PlayerEntity player, InventoryNode part);
    boolean isLocked(PlayerEntity player, InventoryNode part);
    boolean isSlotLocked(PlayerEntity player, int playerSlot);
    boolean isSlotInvisible(PlayerEntity player, int playerSlot);
    InventoryShape getInventoryShape(PlayerEntity player);
}
