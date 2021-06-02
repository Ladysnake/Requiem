/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.impl.remnant.dialogue;

import com.google.common.collect.ImmutableList;
import ladysnake.requiem.api.v1.dialogue.ChoiceResult;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.text.Text;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class DialogueStateMachine implements CutsceneDialogue {

    private final Map<String, DialogueState> states;
    private @Nullable DialogueState currentState;
    private ImmutableList<Text> currentChoices = ImmutableList.of();

    public DialogueStateMachine(DialogueTemplate template) {
        this.states = template.states();
        this.selectState(template.start());
    }

    private DialogueState getCurrentState() {
        return Objects.requireNonNull(this.currentState, () -> this + " has not been initialized !");
    }

    @Override
    public Text getCurrentText() {
        return this.getCurrentState().text();
    }

    @Override
    public ImmutableList<Text> getCurrentChoices() {
        return this.currentChoices;
    }

    @Override
    public ChoiceResult choose(int choice) {
        return this.selectState(this.getCurrentState().getNextState(choice));
    }

    private ChoiceResult selectState(String state) {
        if (!this.states.containsKey(state)) {
            throw new IllegalArgumentException(state + " is not an available dialogue state");
        }
        this.currentState = this.states.get(state);
        this.currentChoices = this.currentState.getAvailableChoices();
        currentState.action().ifPresent(RequiemNetworking::sendDialogueActionMessage);
        return this.currentState.type();
    }

    @Override
    public String toString() {
        return "DialogueStateMachine" + this.states;
    }

}
