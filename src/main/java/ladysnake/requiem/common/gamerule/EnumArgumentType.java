/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.common.gamerule;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.TranslatableText;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class EnumArgumentType<E extends Enum<E>> implements ArgumentType<E> {
    private static final SimpleCommandExceptionType COMMAND_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("creeperspores:argument.enum.invalid"));

    private final Class<E> enumType;
    private final String[] values;
    private final Collection<String> examples;

    public EnumArgumentType(Class<E> enumType) {
        Preconditions.checkArgument(enumType.isEnum());
        this.enumType = enumType;
        this.values = Arrays.stream(enumType.getEnumConstants()).map(Enum::toString).map(s -> s.toLowerCase(Locale.ROOT)).toArray(String[]::new);
        this.examples = Arrays.stream(values).limit(2).collect(Collectors.toList());
    }

    @Override
    public E parse(StringReader reader) throws CommandSyntaxException {
        int startIdx = reader.getCursor();

        if (reader.canRead() && Character.isJavaIdentifierStart(reader.peek())) {
            do {
                reader.skip();
            } while (reader.canRead() && Character.isJavaIdentifierPart(reader.peek()));
        }

        String arg = reader.getString().substring(startIdx, reader.getCursor());

        try {
            return Enum.valueOf(enumType, arg.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw COMMAND_EXCEPTION.createWithContext(reader);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(this.values, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return this.examples;
    }
}
