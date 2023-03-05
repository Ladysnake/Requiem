/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.common.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class RemnantArgumentType implements ArgumentType<RemnantType> {
    public static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(o -> Text.translatable("requiem:remnant_type.not_found", o));
    private static final Collection<String> EXAMPLES = Arrays.asList("requiem:mortal", "remnant");
    private static final SimpleCommandExceptionType BAD_IDENTIFIER = new SimpleCommandExceptionType(Text.translatable("argument.id.invalid"));

    public static RemnantArgumentType remnantType() {
        return new RemnantArgumentType();
    }

    public static RemnantType getRemnantType(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, RemnantType.class);
    }

    private static Identifier parseIdentifier(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while (reader.canRead() && Identifier.isCharValid(reader.peek())) {
            reader.skip();
        }

        String string = reader.getString().substring(i, reader.getCursor());

        try {
            if (string.contains(":")) {
                return new Identifier(string);
            } else {
                return new Identifier(Requiem.MOD_ID + ":" + string);
            }
        } catch (InvalidIdentifierException var4) {
            reader.setCursor(i);
            throw BAD_IDENTIFIER.createWithContext(reader);
        }
    }

    @Override
    public RemnantType parse(StringReader stringReader) throws CommandSyntaxException {
        try {
            return stringReader.readBoolean() ? RemnantTypes.REMNANT : RemnantTypes.MORTAL;
        } catch (CommandSyntaxException e) {
            Identifier identifier = parseIdentifier(stringReader);
            return RequiemRegistries.REMNANT_STATES.getOrEmpty(identifier).orElseThrow(() -> NOT_FOUND_EXCEPTION.create(identifier));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if ("true".startsWith(builder.getRemaining().toLowerCase())) {
            builder.suggest("true");
        }
        if ("false".startsWith(builder.getRemaining().toLowerCase())) {
            builder.suggest("false");
        }
        return CommandSource.suggestIdentifiers(RequiemRegistries.REMNANT_STATES.getIds(), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
