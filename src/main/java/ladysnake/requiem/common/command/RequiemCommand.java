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
package ladysnake.requiem.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TranslatableTextComponent;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RequiemCommand {

    public static final String REQUIEM_ROOT_COMMAND = "requiem";
    public static final String POSSESSION_SUBCOMMAND = "possession";
    public static final String REMNANT_SUBCOMMAND = "remnant";
    public static final String ETHEREAL_SUBCOMMAND = "soul";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(REQUIEM_ROOT_COMMAND)
                .requires(s -> s.hasPermissionLevel(2))
                .then(literal(POSSESSION_SUBCOMMAND)
                        .then(literal("stop")
                                .executes(context -> stopPossession(context.getSource(), Collections.singleton(context.getSource().getPlayer())))
                                .then(argument("target", EntityArgumentType.players())
                                        .executes(context -> stopPossession(context.getSource(), EntityArgumentType.getPlayers(context, "target")))
                                )
                        )
                )
                .then(literal(REMNANT_SUBCOMMAND)
                        .then(literal("query")
                                .executes(context -> queryRemnant(context.getSource(), context.getSource().getPlayer()))
                                .then(argument("target", EntityArgumentType.player())
                                        .executes(context -> queryRemnant(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                                )
                        )
                        .then(literal("set")
                                .then(argument("remnant", BoolArgumentType.bool())
                                        .executes(context -> setRemnant(context.getSource(), Collections.singleton(context.getSource().getPlayer()), BoolArgumentType.getBool(context, "remnant")))
                                        .then(argument("target", EntityArgumentType.players())
                                                .executes(context -> setRemnant(context.getSource(), EntityArgumentType.getPlayers(context, "target"), BoolArgumentType.getBool(context, "remnant")))
                                        )
                                )
                        )
                )
                .then(literal(ETHEREAL_SUBCOMMAND)
                        .then(literal("query")
                                .executes(context -> queryEthereal(context.getSource(), context.getSource().getPlayer()))
                        )
                        .then(literal("set")
                                .then(argument("ethereal", BoolArgumentType.bool())
                                        .executes(context -> setEthereal(context.getSource(), Collections.singleton(context.getSource().getPlayer()), BoolArgumentType.getBool(context, "ethereal")))
                                        .then(argument("target", EntityArgumentType.players())
                                                .executes(context -> setEthereal(context.getSource(), EntityArgumentType.getPlayers(context, "target"), BoolArgumentType.getBool(context, "ethereal")))
                                        )
                                )
                        )
                )
        );
    }

    private static int queryEthereal(ServerCommandSource source, ServerPlayerEntity player) {
        boolean remnant = ((RequiemPlayer) player).getRemnantState().isSoul();
        TextComponent remnantState = new TranslatableTextComponent("requiem:" + (remnant ? "ethereal" : "not_ethereal"));
        source.sendFeedback(new TranslatableTextComponent("requiem:commands.query.success." + (source.getEntity() == player ? "self" : "other"), remnantState), true);
        return remnant ? 1 : 0;
    }

    private static int setEthereal(ServerCommandSource source, Collection<ServerPlayerEntity> players, boolean ethereal) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (((RequiemPlayer) player).getRemnantState().isSoul() != ethereal) {
                ((RequiemPlayer) player).getRemnantState().setSoul(ethereal);
                sendSetEtherealFeedback(source, player, ethereal);
                ++count;
            }
        }
        return count;
    }

    private static void sendSetEtherealFeedback(ServerCommandSource source, ServerPlayerEntity player, boolean ethereal) {
        TextComponent name = new TranslatableTextComponent("requiem:" + (ethereal ? "ethereal" : "not_ethereal"));
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableTextComponent("requiem:commands.ethereal.set.success.self", name), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.appendCommandFeedback(new TranslatableTextComponent("requiem:commands.ethereal.set.target", name));
            }

            source.sendFeedback(new TranslatableTextComponent("requiem:commands.ethereal.set.success.other", player.getDisplayName(), name), true);
        }
    }


    private static int stopPossession(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            PossessionComponent possessionComponent = ((RequiemPlayer) player).getPossessionComponent();
            if (possessionComponent.isPossessing()) {
                Entity possessed = (Entity) Objects.requireNonNull(possessionComponent.getPossessedEntity());
                possessionComponent.stopPossessing();
                sendStopPossessionFeedback(source, player, possessed);
                ++count;
            }
        }
        return count;
    }

    private static void sendStopPossessionFeedback(ServerCommandSource source, ServerPlayerEntity player, Entity possessed) {
        TextComponent name = possessed.getDisplayName();
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableTextComponent("requiem:commands.possession.stop.success.self", name), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.appendCommandFeedback(new TranslatableTextComponent("requiem:commands.possession.stop.target", name));
            }

            source.sendFeedback(new TranslatableTextComponent("requiem:commands.possession.stop.success.other", player.getDisplayName(), name), true);
        }
    }

    private static int queryRemnant(ServerCommandSource source, ServerPlayerEntity player) {
        boolean remnant = ((RequiemPlayer) player).isRemnant();
        TextComponent remnantState = new TranslatableTextComponent("requiem:" + (remnant ? "remnant" : "not_remnant"));
        source.sendFeedback(new TranslatableTextComponent("requiem:commands.query.success." + (source.getEntity() == player ? "self" : "other"), remnantState), true);
        return remnant ? 1 : 0;
    }

    private static int setRemnant(ServerCommandSource source, Collection<ServerPlayerEntity> players, boolean remnant) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (((RequiemPlayer) player).isRemnant() != remnant) {
                ((RequiemPlayer) player).setRemnant(remnant);
                sendSetRemnantFeedback(source, player, remnant);
                ++count;
            }
        }
        return count;
    }

    private static void sendSetRemnantFeedback(ServerCommandSource source, ServerPlayerEntity player, boolean remnant) {
        TextComponent name = new TranslatableTextComponent("requiem:" + (remnant ? "remnant" : "not_remnant"));
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableTextComponent("requiem:commands.remnant.set.success.self", name), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.appendCommandFeedback(new TranslatableTextComponent("requiem:commands.remnant.set.target", name));
            }

            source.sendFeedback(new TranslatableTextComponent("requiem:commands.remnant.set.success.other", player.getDisplayName(), name), true);
        }
    }
}
