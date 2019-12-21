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
package ladysnake.requiem.common.impl.remnant.dialogue;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import ladysnake.requiem.api.v1.annotation.Unlocalized;
import ladysnake.requiem.api.v1.dialogue.ChoiceResult;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DialogueStateMachine implements CutsceneDialogue {
    @SerializedName("start_at")
    private String start;
    private Map<String, DialogueState> states;
    @Nullable
    private transient DialogueState currentState;
    private transient ImmutableList<@Unlocalized String> currentChoices = ImmutableList.of();

    public DialogueStateMachine() {
        this("", new HashMap<>());
    }

    private DialogueStateMachine(String start, Map<String, DialogueState> states) {
        this.start = start;
        this.states = states;
    }

    @Override
    public void start() {
        this.selectState(this.start);
    }

    private DialogueState getCurrentState() {
        return Objects.requireNonNull(this.currentState, "{} has not been initialized !");
    }

    @Override
    public @Unlocalized String getCurrentText() {
        return this.getCurrentState().getText();
    }

    @Override
    public ImmutableList<@Unlocalized String> getCurrentChoices() {
        return this.currentChoices;
    }

    @Override
    public ChoiceResult choose(String choice) {
        return this.selectState(this.getCurrentState().getNextState(choice));
    }

    private ChoiceResult selectState(String state) {
        if (!this.states.containsKey(state)) {
            throw new IllegalArgumentException(state + " is not an available dialogue state");
        }
        this.currentState = this.states.get(state);
        this.currentChoices = this.currentState.getAvailableChoices();
        Identifier action = currentState.getAction();
        if (action != null) {
            RequiemNetworking.sendToServer(RequiemNetworking.createDialogueActionMessage(action));
        }
        return this.currentState.getType();
    }

    public DialogueStateMachine readFromPacket(PacketByteBuf buf) {
        this.start = buf.readString();
        int nbStates = buf.readVarInt();
        this.states = new HashMap<>(nbStates);
        for (int i = 0; i < nbStates; i++) {
            this.states.put(buf.readString(), new DialogueState().readFromPacket(buf));
        }
        return this;
    }

    public void writeToPacket(PacketByteBuf buf) {
        buf.writeString(this.start);
        buf.writeVarInt((byte) this.states.size());
        for (Map.Entry<String, DialogueState> entry : this.states.entrySet()) {
            buf.writeString(entry.getKey());
            entry.getValue().writeToPacket(buf);
        }
    }

    @Override
    public String toString() {
        return "DialogueStateMachine{" +
                "start_at:'" + start + '\'' +
                ", states:" + states +
                '}';
    }
}