/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.common.dialogue;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.api.v1.dialogue.ChoiceResult;
import ladysnake.requiem.core.util.serde.MoreCodecs;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public record DialogueState(
    Text text,
    List<Choice> choices,
    Optional<Identifier> action,
    ChoiceResult type
) {
    static Codec<DialogueState> codec(Codec<JsonElement> jsonCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            MoreCodecs.text(jsonCodec).optionalFieldOf("text", LiteralText.EMPTY).forGetter(DialogueState::text),
            Codec.list(Choice.codec(jsonCodec)).optionalFieldOf("choices", List.of()).forGetter(DialogueState::choices),
            Identifier.CODEC.optionalFieldOf("action").forGetter(DialogueState::action),
            MoreCodecs.enumeration(ChoiceResult.class).optionalFieldOf("type", ChoiceResult.DEFAULT).forGetter(DialogueState::type)
        ).apply(instance, DialogueState::new));
    }

    public ImmutableList<Text> getAvailableChoices() {
        ImmutableList.Builder<Text> builder = ImmutableList.builder();
        for (Choice choice : this.choices) {
            builder.add(choice.text());
        }
        return builder.build();
    }

    public String getNextState(int choice) {
        return this.choices.get(choice).next();
    }

    @Override
    public String toString() {
        String representation = "DialogueState{" +
            "text='" + text + '\'' +
            ", choices=" + choices +
            ", type=" + type;
        if (this.action.isPresent()) {
            representation += ", action=" + action.get();
        }
        return representation + '}';
    }

    public record Choice(Text text, String next) {
        static Codec<Choice> codec(Codec<JsonElement> jsonCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(
                MoreCodecs.text(jsonCodec).fieldOf("text").forGetter(Choice::text),
                Codec.STRING.fieldOf("next").forGetter(Choice::next)
            ).apply(instance, Choice::new));
        }
    }
}
