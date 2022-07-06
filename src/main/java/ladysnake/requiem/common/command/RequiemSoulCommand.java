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
package ladysnake.requiem.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ladysnake.requiem.core.entity.SoulHolderComponent;
import ladysnake.requiem.core.tag.RequiemCoreTags;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RequiemSoulCommand {
    public static final String SOUL_SUBCOMMAND = "soul";

    public static LiteralArgumentBuilder<ServerCommandSource> soulSubcommand() {
        return literal(SOUL_SUBCOMMAND)
            .requires(RequiemCommand.permission("soul.query").or(RequiemCommand.permission("soul.set")))
            // requiem vagrant query [entity]
            .then(literal("query")
                .then(argument("target", EntityArgumentType.entity())
                    .requires(RequiemCommand.permission("soul.query"))
                    .executes(context -> querySoul(context.getSource(), EntityArgumentType.getEntity(context, "target")))
                )
            )
            .then(literal("remove")
                .then(argument("target", EntityArgumentType.entities())
                    .requires(RequiemCommand.permission("soul.remove"))
                    .executes(context -> removeSoul(context.getSource(), EntityArgumentType.getEntities(context, "target"), true))
                )
            )
            .then(literal("restore")
                .then(argument("target", EntityArgumentType.entities())
                    .requires(RequiemCommand.permission("soul.restore"))
                    .executes(context -> removeSoul(context.getSource(), EntityArgumentType.getEntities(context, "target"), false))));
    }

    private static int removeSoul(ServerCommandSource source, Collection<? extends Entity> targets, boolean soulless) {
        int count = 0;
        for (Entity target : targets) {
            if (!(target instanceof LivingEntity living)) {
                throw new CommandException(Text.translatable("requiem:commands.soul.set.fail.not_living", target.getDisplayName()));
            }

            if (target.getType().isIn(RequiemCoreTags.Entity.SOULLESS)) {
                throw new CommandException(Text.translatable("requiem:commands.soul.set.fail.permanently_soulless"));
            }

            if (SoulHolderComponent.isSoulless(living) != soulless) {
                if (soulless) {
                    SoulHolderComponent.get(living).removeSoul();
                    source.sendFeedback(Text.translatable("requiem:commands.soul.remove.success", target.getDisplayName()), true);
                } else {
                    SoulHolderComponent.get(living).giveSoulBack();
                    source.sendFeedback(Text.translatable("requiem:commands.soul.restore.success", target.getDisplayName()), true);
                }

                ++count;
            }
        }
        return count;
    }

    private static int querySoul(ServerCommandSource source, Entity target) {
        boolean hasSoul = target instanceof LivingEntity living && !SoulHolderComponent.isSoulless(living);
        String message = "requiem:commands.soul.query." + (hasSoul ? "has_soul" : "no_soul");
        source.sendFeedback(Text.translatable(message, target.getDisplayName()), true);
        return hasSoul ? 1 : 0;
    }
}
