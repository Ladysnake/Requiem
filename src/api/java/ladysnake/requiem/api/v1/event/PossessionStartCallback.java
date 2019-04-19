/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.event;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface PossessionStartCallback {
    /**
     * Called when a player attempts to possess a mob.
     * <p>
     * The return value of {@link PossessionComponent#startPossessing(MobEntity)}
     * will be {@code true} if the callback returns {@code ALLOW}, {@code false} otherwise.
     * <p>
     * Returning {@link ActionResult#PASS} lets processing continue as normal,
     * calling the next listener. If no callback handles the attempt,
     * the default behaviour of actually possessing the entity takes place.
     *
     * @param target    the possessed entity
     * @param possessor a player triggering a possession attempt
     * @return An {@link ActionResult} describing how the attempt has been handled
     */
    Result onPossessionAttempted(MobEntity target, PlayerEntity possessor);

    Event<PossessionStartCallback> EVENT = EventFactory.createArrayBacked(PossessionStartCallback.class,
            (listeners) -> (target, possessor) -> {
                Result ret = Result.PASS;
                for (PossessionStartCallback listener : listeners) {
                    Result result = listener.onPossessionAttempted(target, possessor);
                    if (result != Result.PASS) {
                        ret = result;
                    }
                    if (result.shortCircuits()) {
                        break;
                    }
                }
                return ret;
            });

    enum Result {
        /**
         * Indicates that this possession attempt cannot succeed.
         * This cancels immediately any further processing.
         */
        DENY,
        /**
         * Let another handler decide.
         * If all handlers return {@code PASS}, the attempt will fail.
         */
        PASS,
        /**
         * Indicates that this possession attempt can succeed.
         * If no handler {@link #DENY denies} the attempt, possession will start.
         */
        ALLOW,
        /**
         * Indicates that this possession attempt has been fully handled by the callback.
         * This cancels immediately any further processing.
         */
        HANDLED;

        public boolean shortCircuits() {
            return this == DENY || this == HANDLED;
        }

        public boolean isSuccess() {
            return this == ALLOW || this == HANDLED;
        }
    }
}
