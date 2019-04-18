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
     * Returning either {@link ActionResult#SUCCESS} {@link ActionResult#FAIL}
     * indicates that this callback has processed the possession attempt,
     * cancelling any further processing. <br>
     * The return value of {@link PossessionComponent#startPossessing(MobEntity)}
     * will be {@code true} if the callback returns {@code SUCCESS}, {@code false} otherwise.
     * <p>
     * Returning {@link ActionResult#PASS} lets processing continue as normal,
     * calling the next listener. If no callback handles the attempt,
     * the default behaviour of actually possessing the entity takes place.
     *
     * @param target    the possessed entity
     * @param possessor a player triggering a possession attempt
     * @return An {@link ActionResult} describing how the attempt has been handled
     */
    ActionResult onPossessionAttempted(MobEntity target, PlayerEntity possessor);

    Event<PossessionStartCallback> EVENT = EventFactory.createArrayBacked(PossessionStartCallback.class,
            (listeners) -> (target, possessor) -> {
                for (PossessionStartCallback listener : listeners) {
                    ActionResult result = listener.onPossessionAttempted(target, possessor);
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });
}
