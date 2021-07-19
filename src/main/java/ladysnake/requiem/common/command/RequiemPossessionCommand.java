/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class RequiemPossessionCommand {
    public static final String POSSESSION_SUBCOMMAND = "possession";

    static LiteralArgumentBuilder<ServerCommandSource> possessionSubcommand() {
        return literal(POSSESSION_SUBCOMMAND)
            .requires(RequiemCommand.permission("possession.start.self").or(RequiemCommand.permission("possession.stop.self")))
            // requiem possession stop [player]
            .then(literal("stop")
                .requires(RequiemCommand.permission("possession.stop.self"))
                .executes(context -> stopPossession(context.getSource(), Collections.singleton(context.getSource().getPlayer())))
                .then(argument("target", EntityArgumentType.players())
                    .requires(RequiemCommand.permission("possession.stop"))
                    .executes(context -> stopPossession(context.getSource(), EntityArgumentType.getPlayers(context, "target")))
                )
            )
            // requiem possession start <possessed> [player]
            .then(literal("start")
                .requires(RequiemCommand.permission("possession.start.self"))
                .then(argument("possessed", EntityArgumentType.entity())
                    .executes(context -> startPossession(context.getSource(), EntityArgumentType.getEntity(context, "possessed"), context.getSource().getPlayer()))
                    .then(argument("possessor", EntityArgumentType.player())
                        .requires(RequiemCommand.permission("possession.start"))
                        .executes(context -> startPossession(context.getSource(), EntityArgumentType.getEntity(context, "possessed"), EntityArgumentType.getPlayer(context, "possessor"))))
                )
            );
    }

    private static int startPossession(ServerCommandSource source, Entity possessed, ServerPlayerEntity player) {
        if (!(possessed instanceof MobEntity)) {
            throw new CommandException(new TranslatableText("requiem:commands.possession.start.fail.not_mob", possessed.getDisplayName()));
        }

        if (!RemnantComponent.get(player).isIncorporeal()) {
            throw new CommandException(new TranslatableText("requiem:commands.possession.start.fail.not_incorporeal", player.getDisplayName()));
        }

        boolean success = PossessionComponent.get(player).startPossessing((MobEntity) possessed);

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
            PossessionComponent possessionComponent = PossessionComponent.get(player);
            Entity host = possessionComponent.getHost();

            if (host != null) {
                possessionComponent.stopPossessing();
                sendStopPossessionFeedback(source, player, host);
                ++count;
            }
        }

        return count;
    }

    private static void sendStopPossessionFeedback(ServerCommandSource source, ServerPlayerEntity player, Entity formerHost) {
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableText("requiem:commands.possession.stop.success.self", formerHost.getDisplayName()), true);
        } else {
            source.sendFeedback(new TranslatableText("requiem:commands.possession.stop.success.other", player.getDisplayName(), formerHost.getDisplayName()), true);
        }
    }
}
