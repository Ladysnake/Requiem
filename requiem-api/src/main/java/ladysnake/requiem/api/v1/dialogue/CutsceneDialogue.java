/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.api.v1.dialogue;

import com.google.common.collect.ImmutableList;
import net.minecraft.text.Text;

public interface CutsceneDialogue {
    void start();

    Text getCurrentText();

    ImmutableList<Text> getCurrentChoices();

    /**
     * Chooses an option in an initialized dialogue
     * @param choice the selected choice
     * @throws IllegalArgumentException if the given choice is not part of the {@link #getCurrentChoices() current choices}
     * @return true if the new state is an end state
     */
    ChoiceResult choose(int choice);
}
