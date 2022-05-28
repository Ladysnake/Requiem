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

public interface RemnantStateChangeCallback {
    void onRemnantStateChange(PlayerEntity player, RemnantComponent state, Cause cause);

    /**
     * Fired after a player becomes or stops being {@link RemnantComponent#isVagrant() vagrant}.
     */
    Event<RemnantStateChangeCallback> EVENT = EventFactory.createArrayBacked(RemnantStateChangeCallback.class,
        (callbacks) -> (player, remnant, cause) -> {
            for (RemnantStateChangeCallback callback : callbacks) {
                callback.onRemnantStateChange(player, remnant, cause);
            }
        });

    enum Cause {
        /**
         * The remnant state changed because the player {@linkplain RemnantComponent#become(RemnantType) became something else}
         */
        TYPE_UPDATE,
        /**
         * The remnant state changed because the player dissociated from its previous body
         */
        DISSOCIATION,
        /**
         * The remnant state changed because the player merged with another body
         */
        MERGE,
        OTHER;

        public boolean isCharacterSwitch() {
            return this == DISSOCIATION || this == MERGE;
        }
    }
}
