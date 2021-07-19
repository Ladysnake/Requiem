package ladysnake.requiem.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RequiemRemnantCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> remnantSubcommand() {
        return literal(RequiemCommand.REMNANT_SUBCOMMAND)
            .requires(RequiemCommand.permission("remnant.query.self").or(RequiemCommand.permission("remnant.set.self")))
            // requiem remnant query [player]
            .then(literal("query")
                .requires(RequiemCommand.permission("remnant.query.self"))
                .executes(context -> queryRemnant(context.getSource(), context.getSource().getPlayer()))
                .then(argument("target", EntityArgumentType.player())
                    .requires(RequiemCommand.permission("remnant.query"))
                    .executes(context -> queryRemnant(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                )
            )
            // requiem remnant set <true|false|identifier> [player]
            .then(literal("set")
                .requires(RequiemCommand.permission("remnant.set.self"))
                .then(argument("remnant_type", RemnantArgumentType.remnantType())
                    .executes(context -> setRemnant(context.getSource(), Collections.singleton(context.getSource().getPlayer()), RemnantArgumentType.getRemnantType(context, "remnant_type")))
                    .then(argument("target", EntityArgumentType.players())
                        .requires(RequiemCommand.permission("remnant.set"))
                        .executes(context -> setRemnant(context.getSource(), EntityArgumentType.getPlayers(context, "target"), RemnantArgumentType.getRemnantType(context, "remnant_type")))
                    )
                )
            );
    }

    private static int queryRemnant(ServerCommandSource source, ServerPlayerEntity player) {
        RemnantType remnantState = RemnantComponent.get(player).getRemnantType();
        source.sendFeedback(new TranslatableText("requiem:commands.query.success." + (source.getEntity() == player ? "self" : "other"), remnantState.getName(), player.getDisplayName()), true);
        return remnantState.isDemon() ? 1 : 0;
    }

    static int setRemnant(ServerCommandSource source, Collection<ServerPlayerEntity> players, RemnantType type) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            if (RemnantComponent.get(player).getRemnantType() != type) {
                RemnantComponent.get(player).become(type);
                sendSetRemnantFeedback(source, player, type);
                ++count;
            }
        }
        return count;
    }

    private static void sendSetRemnantFeedback(ServerCommandSource source, ServerPlayerEntity player, RemnantType type) {
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableText("requiem:commands.remnant.set.success.self", type.getName()), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendSystemMessage(new TranslatableText("requiem:commands.remnant.set.target", type.getName()), Util.NIL_UUID);
            }

            source.sendFeedback(new TranslatableText("requiem:commands.remnant.set.success.other", player.getDisplayName(), type.getName()), true);
        }
    }
}
