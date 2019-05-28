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
package ladysnake.requiem.api.v1;

import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Implemented by {@link PlayerEntity players}.
 */
public interface RequiemPlayer {

    /**
     * @return the player's remnant state
     */
    RemnantState asRemnant();

    /**
     * @return the player's possession component
     */
    PossessionComponent asPossessor();

    PlayerEntity asPlayer();

    void setRemnance(RemnantType type);

    /**
     * @return the player's movement alterer
     */
    MovementAlterer getMovementAlterer();

    boolean isRemnant();

    DeathSuspender getDeathSuspender();

    DialogueTracker getDialogueTracker();

    static RequiemPlayer from(PlayerEntity player) {
        return (RequiemPlayer) player;
    }

    static Optional<RequiemPlayer> fromSafely(@Nullable PlayerEntity player) {
        if (player instanceof RequiemPlayer) {
            return Optional.of(RequiemPlayer.from(player));
        }
        return Optional.empty();
    }
}
