package ladysnake.requiem.common.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RequiemEtherealCommand {
    public static final String ETHEREAL_SUBCOMMAND = "soul";

    static LiteralArgumentBuilder<ServerCommandSource> etherealSubcommand() {
        return literal(ETHEREAL_SUBCOMMAND)
            .requires(RequiemCommand.permission("soul.query.self").or(RequiemCommand.permission("soul.set.self")))
            // requiem soul query [player]
            .then(literal("query")
                .requires(RequiemCommand.permission("soul.query.self"))
                .executes(context -> queryEthereal(context.getSource(), context.getSource().getPlayer()))
                .then(argument("target", EntityArgumentType.player())
                    .requires(RequiemCommand.permission("soul.query"))
                    .executes(context -> queryEthereal(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                )
            )
            // requiem soul set <true|false> [player]
            .then(literal("set")
                .requires(RequiemCommand.permission("soul.set.self"))
                .then(argument("ethereal", BoolArgumentType.bool())
                    .executes(context -> setEthereal(context.getSource(), Collections.singleton(context.getSource().getPlayer()), BoolArgumentType.getBool(context, "ethereal")))
                    .then(argument("target", EntityArgumentType.players())
                        .requires(RequiemCommand.permission("soul.set"))
                        .executes(context -> setEthereal(context.getSource(), EntityArgumentType.getPlayers(context, "target"), BoolArgumentType.getBool(context, "ethereal")))
                    )
                )
            );
    }

    private static int queryEthereal(ServerCommandSource source, ServerPlayerEntity player) {
        boolean remnant = RemnantComponent.get(player).isVagrant();
        Text remnantState = new TranslatableText("requiem:" + (remnant ? "ethereal" : "not_ethereal"));
        source.sendFeedback(new TranslatableText("requiem:commands.query.success." + (source.getEntity() == player ? "self" : "other"), remnantState), true);
        return remnant ? 1 : 0;
    }

    private static int setEthereal(ServerCommandSource source, Collection<ServerPlayerEntity> players, boolean ethereal) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (RemnantComponent.get(player).isVagrant() != ethereal) {
                if (!isRemnant(player)) {
                    throw new CommandException(new TranslatableText("requiem:commands.ethereal.set.fail.mortal", player.getDisplayName()));
                }
                if (!RemnantComponent.get(player).setVagrant(ethereal)) {
                    throw new CommandException(new TranslatableText("requiem:commands.ethereal.set.fail", player.getDisplayName()));
                }
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

    private static boolean isRemnant(ServerPlayerEntity player) {
        return RemnantComponent.get(player).getRemnantType().isDemon();
    }
}
