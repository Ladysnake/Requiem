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
package ladysnake.requiem.api.v1;

import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Contract;

/**
 * A player with extended capabilities allowing interaction with specific Requiem
 * functionality. When an API provider is installed, every {@link PlayerEntity}
 * implements this interface.
 *
 * @since 1.0.0
 */
public interface RequiemPlayer {

    /**
     * Return the {@link MovementAlterer} altering this player's movement.
     * @return the player's {@link MovementAlterer}
     * @since 1.0.0
     */
    @Contract(pure = true)
    MovementAlterer getMovementAlterer();

    /**
     * Return the {@link DialogueTracker} handling cutscene dialogues for this player.
     * @return the player's {@link DialogueTracker}
     * @since 1.0.0
     */
    @Contract(pure = true)
    DialogueTracker getDialogueTracker();

}
