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
package ladysnake.requiem.mixin.command;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public abstract class EntitySelectorOptionsMixin {
    @Shadow
    private static native void putOption(String id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description);

    @Inject(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/command/EntitySelectorOptions;putOption(Ljava/lang/String;Lnet/minecraft/command/EntitySelectorOptions$SelectorHandler;Ljava/util/function/Predicate;Lnet/minecraft/text/Text;)V",
            ordinal = 20,
            shift = At.Shift.AFTER
        )
    )
    private static void registerRequiemSelectorOptions(CallbackInfo ci) {
        putOption("requiem:possessor", reader -> {
            boolean negated = reader.readNegationCharacter();
            String expectedName = reader.getReader().readString();
            reader.setPredicate((entity) -> {
                if (!(entity instanceof Possessable)) {
                    return false;
                } else {
                    PlayerEntity possessor = ((Possessable) entity).getPossessor();
                    String possessorName = possessor == null ? "" : possessor.getName().asString();
                    return possessorName.equals(expectedName) != negated;
                }
            });
        }, (reader) -> true, new TranslatableText("requiem:argument.entity.options.possessor.description"));
    }

    @ModifyArg(method = "suggestOptions", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;suggest(Ljava/lang/String;Lcom/mojang/brigadier/Message;)Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;"))
    private static String suggestQuotes(String text) {
        if (text.indexOf(':') > 0 && text.indexOf('"') < 0 && text.indexOf('=') == text.length() - 1) {
            return '"' + text.substring(0, text.length() -1) + "\"=";
        }
        return text;
    }
}
