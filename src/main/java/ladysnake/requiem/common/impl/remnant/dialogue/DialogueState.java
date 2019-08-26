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
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.dialogue.ChoiceResult;
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
    private ChoiceResult type;

    @CalledThroughReflection
    public DialogueState() {
        this("", new LinkedHashMap<>(), null, ChoiceResult.DEFAULT);
    }

    private DialogueState(String text, LinkedHashMap<String, String> choices, @Nullable Identifier action, ChoiceResult type) {
        this.text = text;
        this.choices = choices;
        this.action = action;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public ImmutableList<String> getAvailableChoices() {
        return ImmutableList.copyOf(choices.keySet());
    }

    public String getNextState(String choice) {
        return this.choices.get(choice);
    }

    public ChoiceResult getType() {
        return type;
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
        this.type = buf.readEnumConstant(ChoiceResult.class);
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
        buf.writeEnumConstant(this.type);
    }

    @Override
    public String toString() {
        String representation = "DialogueState{" +
                "text='" + text + '\'' +
                ", choices=" + choices +
                ", type=" + type;
        if (this.action != null) {
            representation += ", action=" + action;
        }
        return representation + '}';
    }
}
