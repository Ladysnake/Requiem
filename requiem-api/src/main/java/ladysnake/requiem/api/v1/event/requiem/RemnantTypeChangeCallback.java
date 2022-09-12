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

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

/**
 * @since 2.0.0
 */
public interface RemnantTypeChangeCallback {
    void onRemnantTypeChange(PlayerEntity player, RemnantType oldType, RemnantType newType);

    /**
     * Fired after a player changes their {@link RemnantComponent#getRemnantType() remnant type}.
     */
    Event<RemnantTypeChangeCallback> EVENT = EventFactory.createArrayBacked(RemnantTypeChangeCallback.class,
        (callbacks) -> (player, oldType, newType) -> {
            for (RemnantTypeChangeCallback callback : callbacks) {
                callback.onRemnantTypeChange(player, oldType, newType);
            }
        });
}
