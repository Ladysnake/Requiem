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
package ladysnake.requiem.api.v1.event.requiem;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PossessionEvents {
    public static final Event<InventoryTransferCheck> INVENTORY_TRANSFER_CHECK = EventFactory.createArrayBacked(InventoryTransferCheck.class,
        callbacks -> (possessor, host) -> {
            TriState ret = TriState.DEFAULT;
            for (InventoryTransferCheck callback : callbacks) {
                switch (callback.shouldTransfer(possessor, host)) {
                    case TRUE -> ret = TriState.TRUE;
                    case FALSE -> {
                        return TriState.FALSE;
                    }
                }
            }
            return ret;
        });

    public interface InventoryTransferCheck {
        TriState shouldTransfer(ServerPlayerEntity possessor, LivingEntity host);
    }
}
