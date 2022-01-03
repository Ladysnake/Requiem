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
package ladysnake.requiem.api.v1.event.minecraft;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface BlockReplacedCallback {
    Event<BlockReplacedCallback> EVENT = EventFactory.createArrayBacked(BlockReplacedCallback.class,
        (callbacks) -> (oldState, world, pos, newState, moved) -> {
            for (BlockReplacedCallback callback : callbacks) {
                callback.onBlockPlaced(oldState, world, pos, newState, moved);
            }
        });

    void onBlockPlaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean moved);
}
