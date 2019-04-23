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
package ladysnake.requiem.api.v1.remnant;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public interface RemnantState {
    String NULL_STATE_ID = "requiem:mortal";

    boolean isIncorporeal();

    boolean isSoul();

    void setSoul(boolean incorporeal);

    RemnantType getType();

    /**
     * Called when this remnant state's player is cloned
     *
     * @param clone the player's clone
     * @param dead true if the original player is dead, false otherwise
     */
    void onPlayerClone(ServerPlayerEntity clone, boolean dead);

    CompoundTag toTag(CompoundTag tag);

    void fromTag(CompoundTag tag);

    default void update() {
        // NO-OP
    }

    /**
     * A predicate matching entities that are remnant
     */
    Predicate<Entity> REMNANT = e -> e instanceof RequiemPlayer && ((RequiemPlayer) e).isRemnant();

    /**
     * Helper method to get the remnant state of an entity if it exists
     *
     * @param entity a possibly remnant entity
     * @return the remnant handler of that entity
     */
    static Optional<RemnantState> getIfRemnant(@Nullable Entity entity) {
        if (REMNANT.test(entity)) {
            //The predicate guarantees that the entity is not null
            //noinspection ConstantConditions
            return Optional.of(((RequiemPlayer) entity).getRemnantState());
        }
        return Optional.empty();
    }
}
