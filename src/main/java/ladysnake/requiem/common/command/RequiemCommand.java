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

import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.literal;

public final class RequiemCommand {

    public static final String REQUIEM_ROOT_COMMAND = "requiem";

    private static final Set<String> permissions = new HashSet<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(REQUIEM_ROOT_COMMAND)
            .requires(RequiemCommand::checkPermissions)
            .then(RequiemEtherealCommand.etherealSubcommand())
            .then(RequiemPossessionCommand.possessionSubcommand())
            .then(RequiemRemnantCommand.remnantSubcommand())
            .then(RequiemShellCommand.shellSubcommand())
            .then(RequiemSoulCommand.soulSubcommand())
        );
    }

    public static Predicate<ServerCommandSource> permission(String name) {
        String perm = "requiem.command" + name;
        permissions.add(perm);
        return Permissions.require(perm, 2);
    }

    private static boolean checkPermissions(ServerCommandSource source) {
        if (source.hasPermissionLevel(2)) return true;

        for (String perm : permissions) {
            if (Permissions.check(source, perm)) {
                return true;
            }
        }
        return false;
    }

    public static <T> int runOne(T element, Consumer<T> action) {
        action.accept(element);
        return 1;
    }

    public static <T> int runMany(Collection<T> collection, Consumer<T> action) {
        int count = 0;
        for (T element : collection) {
            action.accept(element);
            count++;
        }
        return count;
    }
}
