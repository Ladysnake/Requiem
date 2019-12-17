/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

public interface RemnantState {
    String NULL_STATE_ID = "requiem:mortal";

    /**
     * Return whether this player is currently incorporeal.
     * A player is considered incorporeal if its current corporeality
     * is not tangible and they have no surrogate body.
     * @return true if the player is currently incorporeal, {@code false} otherwise
     */
    boolean isIncorporeal();

    boolean isSoul();

    void setSoul(boolean incorporeal);

    RemnantType getType();

    /**
     * Called when this remnant state's player is cloned
     *
     * @param original the player's clone
     * @param lossless false if the original player is dead, true otherwise
     */
    void copyFrom(ServerPlayerEntity original, boolean lossless);

    CompoundTag toTag(CompoundTag tag);

    void fromTag(CompoundTag tag);

    default void update() {
        // NO-OP
    }

}
