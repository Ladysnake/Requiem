package ladysnake.requiem.common.impl.remnant.dialogue;

import com.google.common.collect.ImmutableList;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class DialogueState {
    private String text;
    private LinkedHashMap<String, String> choices;
    @Nullable
    private Identifier action;
    private boolean end;

    @CalledThroughReflection
    public DialogueState() {
        this("", new LinkedHashMap<>(), null, false);
    }

    private DialogueState(String text, LinkedHashMap<String, String> choices, @Nullable Identifier action, boolean end) {
        this.text = text;
        this.choices = choices;
        this.action = action;
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public ImmutableList<String> getAvailableChoices() {
        return ImmutableList.copyOf(choices.keySet());
    }

    public boolean isEnd() {
        return end;
    }

    @Nullable
    public Identifier getAction() {
        return action;
    }

    public DialogueState readFromPacket(PacketByteBuf buf) {
        this.text = buf.readString();
        int nbChoices = buf.readByte();
        this.choices = new LinkedHashMap<>(nbChoices);
        for (int i = 0; i < nbChoices; i++) {
            choices.put(buf.readString(), buf.readString());
        }
        String actionStr = buf.readString();
        if (!actionStr.isEmpty()) {
            this.action = new Identifier(actionStr);
        }
        this.end = buf.readBoolean();
        return this;
    }

    public void writeToPacket(PacketByteBuf buf) {
        buf.writeString(this.text);
        buf.writeByte((byte)this.choices.size());
        for (Map.Entry<String, String> choice : this.choices.entrySet()) {
            buf.writeString(choice.getKey());
            buf.writeString(choice.getValue());
        }
        buf.writeString(this.action == null ? "" : this.action.toString());
        buf.writeBoolean(this.end);
    }

    @Override
    public String toString() {
        String representation = "DialogueState{" +
                "text='" + text + '\'' +
                ", choices=" + choices +
                ", end=" + end;
        if (this.action != null) {
            representation += ", action=" + action;
        }
        return representation + '}';
    }
}
