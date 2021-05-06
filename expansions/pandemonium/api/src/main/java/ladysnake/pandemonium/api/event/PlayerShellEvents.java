/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.pandemonium.api.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public final class PlayerShellEvents {
    /**
     * Fired after a player has split into a soul and a shell
     */
    public static final Event<Split> PLAYER_SPLIT = EventFactory.createArrayBacked(Split.class, (callbacks) -> (whole, soul, playerShell) -> {
        for (Split callback : callbacks) {
            callback.onPlayerSplit(whole, soul, playerShell);
        }
    });

    /**
     * Fired after a soul has merged with a shell
     */
    public static final Event<Merge> PLAYER_MERGED = EventFactory.createArrayBacked(Merge.class, (callbacks) -> (player, playerShell, shellProfile) -> {
        for (Merge callback : callbacks) {
            callback.onPlayerMerge(player, playerShell, shellProfile);
        }
    });

    @FunctionalInterface
    public interface Split {
        /**
         * Fired when a shell is being created.
         *
         * <p>If the shell is created as part of regular gameplay or using the "/pandemonium shell split"
         * command, {@code whole} and {@code soul} will be 2 different entities ({@code whole != soul},
         * with the former being now removed from the world, and with the latter being now added to the world.
         * In this scenario, callbacks are free to modify the state of any parameter.
         *
         * <p>If the shell is created through the use of the "/pandemonium shell create" command,
         * {@code whole} and {@code soul} will refer to the same player, left unaffected by the command.
         * In this scenario, callbacks should avoid modifying any state except for the shell itself.
         *
         * <p>In either case, the {@code soul} is guaranteed to be existing in the world
         * when this event is fired.
         *
         * @param whole       the player before it started splitting
         * @param soul        the respawned player, may be the same as {@code whole}
         * @param playerShell the shell being left behind
         */
        void onPlayerSplit(ServerPlayerEntity whole, ServerPlayerEntity soul, ServerPlayerEntity playerShell);
    }

    @FunctionalInterface
    public interface Merge {
        /**
         * @param player       the player merging with the shell
         * @param playerShell  the shell being merged with
         * @param shellProfile the profile of the player which created this shell
         */
        void onPlayerMerge(ServerPlayerEntity player, ServerPlayerEntity playerShell, GameProfile shellProfile);
    }
}
