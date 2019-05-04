package ladysnake.requiem.common.impl.remnant.dialogue;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import ladysnake.requiem.api.v1.annotation.Unlocalized;
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
    public void init() {
        this.choose(this.start);
    }

    private DialogueState getCurrentState() {
        return Objects.requireNonNull(this.currentState, "{} has not been initialized !");
    }

    @Override
    public @Unlocalized String getCurrentText() {
        return this.currentState.getText();
    }

    @Override
    public ImmutableList<@Unlocalized String> getCurrentChoices() {
        return this.currentChoices;
    }

    @Override
    public void choose(String choice) {
        if (!this.states.containsKey(choice)) {
            throw new IllegalArgumentException(choice + " is not an available dialogue option");
        }
        this.currentState = this.states.get(choice);
        this.currentChoices = this.currentState.getAvailableChoices();
        Identifier action = currentState.getAction();
        if (action != null) {
            RequiemNetworking.sendToServer(RequiemNetworking.createDialogueActionMessage(action));
        }
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
