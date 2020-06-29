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
package ladysnake.requiem.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.command.CommandException;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

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
                        // requiem possession stop [player]
                        .then(literal("stop")
                                .executes(context -> stopPossession(context.getSource(), Collections.singleton(context.getSource().getPlayer())))
                                .then(argument("target", EntityArgumentType.players())
                                        .executes(context -> stopPossession(context.getSource(), EntityArgumentType.getPlayers(context, "target")))
                                )
                        )
                        // requiem possession start <possessed> [player]
                        .then(literal("start")
                                .then(argument("possessed", EntityArgumentType.entity())
                                        .executes(context -> startPossession(context.getSource(), EntityArgumentType.getEntity(context, "possessed"), context.getSource().getPlayer()))
                                        .then(argument("possessor", EntityArgumentType.player())
                                                .executes(context -> startPossession(context.getSource(), EntityArgumentType.getEntity(context, "possessed"), EntityArgumentType.getPlayer(context, "possessor"))))
                                )
                        )
                )
                .then(literal(REMNANT_SUBCOMMAND)
                        // requiem remnant query [player]
                        .then(literal("query")
                                .executes(context -> queryRemnant(context.getSource(), context.getSource().getPlayer()))
                                .then(argument("target", EntityArgumentType.player())
                                        .executes(context -> queryRemnant(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                                )
                        )
                        // requiem remnant set <true|false> [player]
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
                        // requiem soul query [player]
                        .then(literal("query")
                                .executes(context -> queryEthereal(context.getSource(), context.getSource().getPlayer()))
                        )
                        // requiem soul set <true|false> [player]
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
        boolean remnant = ((RequiemPlayer) player).asRemnant().isSoul();
        Text remnantState = new TranslatableText("requiem:" + (remnant ? "ethereal" : "not_ethereal"));
        source.sendFeedback(new TranslatableText("requiem:commands.query.success." + (source.getEntity() == player ? "self" : "other"), remnantState), true);
        return remnant ? 1 : 0;
    }

    private static int setEthereal(ServerCommandSource source, Collection<ServerPlayerEntity> players, boolean ethereal) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (((RequiemPlayer) player).asRemnant().isSoul() != ethereal) {
                if (!isRemnant(player)) {
                    throw new CommandException(new TranslatableText("requiem:commands.ethereal.set.fail", player.getDisplayName()));
                }
                ((RequiemPlayer) player).asRemnant().setSoul(ethereal);
                sendSetEtherealFeedback(source, player, ethereal);
                ++count;
            }
        }
        return count;
    }

    private static void sendSetEtherealFeedback(ServerCommandSource source, ServerPlayerEntity player, boolean ethereal) {
        Text name = new TranslatableText("requiem:" + (ethereal ? "ethereal" : "not_ethereal"));
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableText("requiem:commands.ethereal.set.success.self", name), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendSystemMessage(new TranslatableText("requiem:commands.ethereal.set.target", name), Util.NIL_UUID);
            }

            source.sendFeedback(new TranslatableText("requiem:commands.ethereal.set.success.other", player.getDisplayName(), name), true);
        }
    }

    private static int startPossession(ServerCommandSource source, Entity possessed, ServerPlayerEntity player) {
        if (!(possessed instanceof MobEntity)) {
            throw new CommandException(new TranslatableText("requiem:commands.possession.start.fail.not_mob", possessed.getDisplayName()));
        }
        if (!((RequiemPlayer) player).asRemnant().isIncorporeal()) {
            throw new CommandException(new TranslatableText("requiem:commands.possession.start.fail.not_incorporeal", player.getDisplayName()));
        }
        boolean success = ((RequiemPlayer) player).asPossessor().startPossessing((MobEntity) possessed);
        if (!success) {
            throw new CommandException(new TranslatableText("requiem:commands.possession.start.fail", possessed.getDisplayName()));
        }
        TranslatableText message;
        String baseKey = "requiem:commands.possession.start.success";
        if (source.getEntity() == player) {
            message = new TranslatableText(baseKey + ".self", possessed.getDisplayName());
        } else {
            message = new TranslatableText(baseKey + ".other", player.getDisplayName(), possessed.getDisplayName());
        }
        source.sendFeedback(message, true);
        return 1;
    }

    private static int stopPossession(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            PossessionComponent possessionComponent = ((RequiemPlayer) player).asPossessor();
            if (possessionComponent.isPossessing()) {
                Entity possessed = Objects.requireNonNull(possessionComponent.getPossessedEntity());
                possessionComponent.stopPossessing();
                sendStopPossessionFeedback(source, player, possessed);
                ++count;
            }
        }
        return count;
    }

    private static void sendStopPossessionFeedback(ServerCommandSource source, ServerPlayerEntity player, Entity possessed) {
        Text name = possessed.getDisplayName();
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableText("requiem:commands.possession.stop.success.self", name), true);
        } else {
            source.sendFeedback(new TranslatableText("requiem:commands.possession.stop.success.other", player.getDisplayName(), name), true);
        }
    }

    private static int queryRemnant(ServerCommandSource source, ServerPlayerEntity player) {
        boolean remnant = isRemnant(player);
        Text remnantState = new TranslatableText("requiem:" + (remnant ? "remnant" : "not_remnant"));
        source.sendFeedback(new TranslatableText("requiem:commands.query.success." + (source.getEntity() == player ? "self" : "other"), remnantState, player.getDisplayName()), true);
        return remnant ? 1 : 0;
    }

    private static int setRemnant(ServerCommandSource source, Collection<ServerPlayerEntity> players, boolean remnant) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (isRemnant(player) != remnant) {
                RemnantType remnance = remnant ? RemnantTypes.REMNANT : RemnantTypes.MORTAL;
                ((RequiemPlayer) player).become(remnance);
                sendSetRemnantFeedback(source, player, remnant);
                ++count;
            }
        }
        return count;
    }

    private static boolean isRemnant(ServerPlayerEntity player) {
        return RequiemPlayer.from(player).asRemnant().getType().isDemon();
    }

    private static void sendSetRemnantFeedback(ServerCommandSource source, ServerPlayerEntity player, boolean remnant) {
        Text name = new TranslatableText("requiem:" + (remnant ? "remnant" : "not_remnant"));
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableText("requiem:commands.remnant.set.success.self", name), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendSystemMessage(new TranslatableText("requiem:commands.remnant.set.target", name), Util.NIL_UUID);
            }

            source.sendFeedback(new TranslatableText("requiem:commands.remnant.set.success.other", player.getDisplayName(), name), true);
        }
    }
}
