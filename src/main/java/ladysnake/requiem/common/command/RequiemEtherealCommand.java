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

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RequiemEtherealCommand {
    public static final String ETHEREAL_SUBCOMMAND = "vagrant";

    static LiteralArgumentBuilder<ServerCommandSource> etherealSubcommand() {
        return literal(ETHEREAL_SUBCOMMAND)
            .requires(RequiemCommand.permission("vagrant.query.self").or(RequiemCommand.permission("vagrant.set.self")))
            // requiem vagrant query [player]
            .then(literal("query")
                .requires(RequiemCommand.permission("vagrant.query.self"))
                .executes(context -> queryEthereal(context.getSource(), context.getSource().getPlayer()))
                .then(argument("target", EntityArgumentType.player())
                    .requires(RequiemCommand.permission("vagrant.query"))
                    .executes(context -> queryEthereal(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                )
            )
            // requiem vagrant set <true|false> [player]
            .then(literal("set")
                .requires(RequiemCommand.permission("vagrant.set.self"))
                .then(argument("ethereal", BoolArgumentType.bool())
                    .executes(context -> setEthereal(context.getSource(), Collections.singleton(context.getSource().getPlayer()), BoolArgumentType.getBool(context, "ethereal")))
                    .then(argument("target", EntityArgumentType.players())
                        .requires(RequiemCommand.permission("vagrant.set"))
                        .executes(context -> setEthereal(context.getSource(), EntityArgumentType.getPlayers(context, "target"), BoolArgumentType.getBool(context, "ethereal")))
                    )
                )
            );
    }

    private static int queryEthereal(ServerCommandSource source, ServerPlayerEntity player) {
        boolean remnant = RemnantComponent.get(player).isVagrant();
        Text remnantState = Text.translatable("requiem:" + (remnant ? "ethereal" : "not_ethereal"));
        source.sendFeedback(Text.translatable("requiem:commands.query.success." + (source.getEntity() == player ? "self" : "other"), remnantState), true);
        return remnant ? 1 : 0;
    }

    private static int setEthereal(ServerCommandSource source, Collection<ServerPlayerEntity> players, boolean ethereal) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (RemnantComponent.get(player).isVagrant() != ethereal) {
                if (!isRemnant(player)) {
                    throw new CommandException(Text.translatable("requiem:commands.ethereal.set.fail.mortal", player.getDisplayName()));
                }
                if (!RemnantComponent.get(player).setVagrant(ethereal)) {
                    throw new CommandException(Text.translatable("requiem:commands.ethereal.set.fail", player.getDisplayName()));
                }
                sendSetEtherealFeedback(source, player, ethereal);
                ++count;
            }
        }
        return count;
    }

    private static void sendSetEtherealFeedback(ServerCommandSource source, ServerPlayerEntity player, boolean ethereal) {
        Text name = Text.translatable("requiem:" + (ethereal ? "ethereal" : "not_ethereal"));
        if (source.getEntity() == player) {
            source.sendFeedback(Text.translatable("requiem:commands.ethereal.set.success.self", name), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendSystemMessage(Text.translatable("requiem:commands.ethereal.set.target", name));
            }

            source.sendFeedback(Text.translatable("requiem:commands.ethereal.set.success.other", player.getDisplayName(), name), true);
        }
    }

    private static boolean isRemnant(ServerPlayerEntity player) {
        return RemnantComponent.get(player).getRemnantType().isDemon();
    }
}
