package ladysnake.dissolution.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
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

public class DissolutionCommand {

    public static final String DISSOLUTION_ROOT_COMMAND = "dissolution";
    public static final String POSSESSION_SUBCOMMAND = "possession";
    public static final String REMNANT_SUBCOMMAND = "remnant";
    public static final String ETHEREAL_SUBCOMMAND = "soul";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(DISSOLUTION_ROOT_COMMAND)
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
        boolean remnant = ((DissolutionPlayer) player).getRemnantState().isSoul();
        TextComponent remnantState = new TranslatableTextComponent("dissolution:" + (remnant ? "ethereal" : "not_ethereal"));
        source.sendFeedback(new TranslatableTextComponent("dissolution:commands.query.success." + (source.getEntity() == player ? "self" : "other"), remnantState), true);
        return remnant ? 1 : 0;
    }

    private static int setEthereal(ServerCommandSource source, Collection<ServerPlayerEntity> players, boolean ethereal) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (((DissolutionPlayer) player).getRemnantState().isSoul() != ethereal) {
                ((DissolutionPlayer) player).getRemnantState().setSoul(ethereal);
                sendSetEtherealFeedback(source, player, ethereal);
                ++count;
            }
        }
        return count;
    }

    private static void sendSetEtherealFeedback(ServerCommandSource source, ServerPlayerEntity player, boolean ethereal) {
        TextComponent name = new TranslatableTextComponent("dissolution:" + (ethereal ? "ethereal" : "not_ethereal"));
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableTextComponent("dissolution:commands.ethereal.set.success.self", name), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.appendCommandFeedback(new TranslatableTextComponent("dissolution:commands.ethereal.set.target", name));
            }

            source.sendFeedback(new TranslatableTextComponent("dissolution:commands.ethereal.set.success.other", player.getDisplayName(), name), true);
        }
    }


    private static int stopPossession(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            PossessionComponent possessionComponent = ((DissolutionPlayer) player).getPossessionComponent();
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
            source.sendFeedback(new TranslatableTextComponent("dissolution:commands.possession.stop.success.self", name), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.appendCommandFeedback(new TranslatableTextComponent("dissolution:commands.possession.stop.target", name));
            }

            source.sendFeedback(new TranslatableTextComponent("dissolution:commands.possession.stop.success.other", player.getDisplayName(), name), true);
        }
    }

    private static int queryRemnant(ServerCommandSource source, ServerPlayerEntity player) {
        boolean remnant = ((DissolutionPlayer) player).isRemnant();
        TextComponent remnantState = new TranslatableTextComponent("dissolution:" + (remnant ? "remnant" : "not_remnant"));
        source.sendFeedback(new TranslatableTextComponent("dissolution:commands.query.success." + (source.getEntity() == player ? "self" : "other"), remnantState), true);
        return remnant ? 1 : 0;
    }

    private static int setRemnant(ServerCommandSource source, Collection<ServerPlayerEntity> players, boolean remnant) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (((DissolutionPlayer) player).isRemnant() != remnant) {
                ((DissolutionPlayer) player).setRemnant(remnant);
                sendSetRemnantFeedback(source, player, remnant);
                ++count;
            }
        }
        return count;
    }

    private static void sendSetRemnantFeedback(ServerCommandSource source, ServerPlayerEntity player, boolean remnant) {
        TextComponent name = new TranslatableTextComponent("dissolution:" + (remnant ? "remnant" : "not_remnant"));
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableTextComponent("dissolution:commands.remnant.set.success.self", name), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.appendCommandFeedback(new TranslatableTextComponent("dissolution:commands.remnant.set.target", name));
            }

            source.sendFeedback(new TranslatableTextComponent("dissolution:commands.remnant.set.success.other", player.getDisplayName(), name), true);
        }
    }
}
