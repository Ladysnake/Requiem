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
package ladysnake.requiem.api.v1.dialogue;

import net.minecraft.server.network.ServerPlayerEntity;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@FunctionalInterface
@API(status = EXPERIMENTAL)
public interface DialogueAction {
    /**
     * Handles a dialogue action triggered by the given player.
     * <p>
     * There is no guarantee that the game is in the desired state at
     * the time this method is called, as such it should do all necessary
     * checks to prevent possible exploits.
     *
     * @param player the player executing the action
     */
    void handle(ServerPlayerEntity player);

    DialogueAction NONE = (p) -> {};
}
