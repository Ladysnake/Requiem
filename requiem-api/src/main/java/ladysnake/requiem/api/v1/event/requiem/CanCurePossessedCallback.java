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
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.LivingEntity;

@FunctionalInterface
public interface CanCurePossessedCallback {
    /**
     * Checks if a LivingEntity can be cured. This is checked on start of the possession, and the end.
     * If any callback returns {@link TriState#FALSE}, it is assumed the entity cannot be cured. Use {@link TriState#FALSE} sparingly.
     * If your callback returns {@link TriState#TRUE}, it is not guaranteed that it will cure. <tt>It also may cause a crash if the curing isn't handled with {@link}</tt>
     * @param body the potentially curable LivingEntity
    **/
    TriState canCurePossessed(LivingEntity body);

    Event<CanCurePossessedCallback> EVENT = EventFactory.createArrayBacked(CanCurePossessedCallback.class,
        callbacks -> (body) -> {
            TriState storedState = TriState.DEFAULT;
            for (CanCurePossessedCallback callback : callbacks) {
                TriState state = callback.canCurePossessed(body);
                switch (state) {
                    case FALSE:
                        return TriState.FALSE;
                    case TRUE:
                        storedState = TriState.TRUE;
                }
            }
            return storedState;
        });
}
