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
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.dialogue.ChoiceResult;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class DialogueState {
    private Text text;
    private List<Choice> choices;
    @Nullable
    private Identifier action;
    private ChoiceResult type;

    @CalledThroughReflection
    public DialogueState() {
        this(LiteralText.EMPTY, Collections.emptyList(), null, ChoiceResult.DEFAULT);
    }

    private DialogueState(Text text, List<Choice> choices, @Nullable Identifier action, ChoiceResult type) {
        this.text = text;
        this.choices = choices;
        this.action = action;
        this.type = type;
    }

    public Text getText() {
        return text;
    }

    public ImmutableList<Text> getAvailableChoices() {
        ImmutableList.Builder<Text> ret = ImmutableList.builder();
        for (Choice choice : this.choices) {
            ret.add(choice.text);
        }
        return ret.build();
    }

    public String getNextState(int choice) {
        return this.choices.get(choice).next;
    }

    public ChoiceResult getType() {
        return type;
    }

    @Nullable
    public Identifier getAction() {
        return action;
    }

    public DialogueState readFromPacket(PacketByteBuf buf) {
        this.text = buf.readText();
        int nbChoices = buf.readByte();
        ImmutableList.Builder<Choice> choices = ImmutableList.builder();
        for (int i = 0; i < nbChoices; i++) {
            choices.add(new Choice(buf.readText(), buf.readString()));
        }
        this.choices = choices.build();
        String actionStr = buf.readString();
        if (!actionStr.isEmpty()) {
            this.action = new Identifier(actionStr);
        }
        this.type = buf.readEnumConstant(ChoiceResult.class);
        return this;
    }

    public void writeToPacket(PacketByteBuf buf) {
        buf.writeText(this.text);
        buf.writeByte((byte)this.choices.size());
        for (Choice choice : this.choices) {
            buf.writeText(choice.text);
            buf.writeString(choice.next);
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

    public static final class Choice {
        private final Text text;
        private final String next;

        private Choice(Text text, String next) {
            this.text = text;
            this.next = next;
        }
    }
}
