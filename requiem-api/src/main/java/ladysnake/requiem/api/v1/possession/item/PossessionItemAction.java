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
package ladysnake.requiem.api.v1.possession.item;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public interface PossessionItemAction {
    /**
     * Called when right-clicking ("using") an item.
     *
     * <p>Upon return:
     * <ul><li>SUCCESS cancels further processing and, on the client, sends a packet to the server.
     * <li>PASS falls back to further processing.
     * <li>FAIL cancels further processing and does not send a packet to the server.</ul>
     */
    TypedActionResult<ItemStack> interact(PlayerEntity player, MobEntity possessed, ItemStack stack, World world, Hand hand);
}
