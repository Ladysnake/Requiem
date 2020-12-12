/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.api.v1.remnant;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Contract;

public interface RemnantState {
    String NULL_STATE_ID = "requiem:mortal";

    /**
     * Return whether this player is currently incorporeal.
     *
     * <p>A player is considered incorporeal if they have neither their natural body or a surrogate one.
     * If this method returns {@code true}, the player is also {@link #isSoul() vagrant}.
     *
     * @return true if the player is currently incorporeal, {@code false} otherwise
     */
    boolean isIncorporeal();

    /**
     * Return whether this player is currently dissociated from a natural player body.
     */
    @Contract(pure = true)
    boolean isSoul();

    boolean setSoul(boolean incorporeal);

    RemnantType getType();

    /**
     * Called when this remnant state's player is cloned
     *
     * @param original the player's clone
     * @param lossless false if the original player is dead, true otherwise
     */
    void prepareRespawn(ServerPlayerEntity original, boolean lossless);
}
