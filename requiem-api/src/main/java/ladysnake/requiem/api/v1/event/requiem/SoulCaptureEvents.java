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
import net.minecraft.entity.LivingEntity;

public final class SoulCaptureEvents {
    public static final Event<BeforeAttempt> BEFORE_ATTEMPT = EventFactory.createArrayBacked(BeforeAttempt.class,
        (callbacks) -> (stealer, target, captureType) -> {
            for (BeforeAttempt callback : callbacks) {
                if (!callback.canAttemptCapturing(stealer, target, captureType)) {
                    return false;
                }
            }
            return true;
        });

    @FunctionalInterface
    public interface BeforeAttempt {
        /**
         * Called before an entity attempts to capture the soul of another.
         *
         * @param stealer the entity attempting the capture. In the base mod, this can be a player or a mortician.
         * @param target the entity on which the capture is attempted.
         * @return {@code true} if the attempt can proceed, {@code false} otherwise
         */
        boolean canAttemptCapturing(LivingEntity stealer, LivingEntity target, CaptureType captureType);
    }

    public enum CaptureType {
        /**
         * A soul capture from a soul aggregate removes a single soul from the whole, dealing damage to the creature
         * and filling the vessel with a random soul
         */
        AGGREGATE,
        /**
         * A soul capture from a regular creature removes its soul, rendering it soulless
         */
        NORMAL
    }
}
