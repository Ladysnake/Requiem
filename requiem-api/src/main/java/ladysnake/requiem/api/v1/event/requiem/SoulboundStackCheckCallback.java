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
package ladysnake.requiem.api.v1.event.requiem;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public interface SoulboundStackCheckCallback {
    /**
     * Checks if the given stack should stay in the player's inventory
     * when leaving a body
     */
    boolean isSoulbound(PlayerInventory inventory, ItemStack stack, int slot);

    Event<SoulboundStackCheckCallback> EVENT = EventFactory.createArrayBacked(SoulboundStackCheckCallback.class, (callbacks) -> (inventory, stack, slot) -> {
        for (SoulboundStackCheckCallback callback : callbacks) {
            if (callback.isSoulbound(inventory, stack, slot)) {
                return true;
            }
        }
        return false;
    });
}
