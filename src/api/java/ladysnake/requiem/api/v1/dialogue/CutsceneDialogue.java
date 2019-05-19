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

import com.google.common.collect.ImmutableList;
import ladysnake.requiem.api.v1.annotation.Unlocalized;

public interface CutsceneDialogue {
    void start();

    @Unlocalized String getCurrentText();

    ImmutableList<@Unlocalized String> getCurrentChoices();

    /**
     * Chooses an option in an initialized
     * @param choice the selected choice
     * @throws IllegalArgumentException if the given choice is not part of the {@link #getCurrentChoices() current choices}
     * @return true if the new state is an end state
     */
    ChoiceResult choose(String choice);
}
