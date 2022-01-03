/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.api.v1.event.requiem;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public final class PlayerShellEvents {
    public static final Event<DataTransfer> DATA_TRANSFER = EventFactory.createArrayBacked(DataTransfer.class, (callbacks) -> (from, to, merge) -> {
        for (DataTransfer callback : callbacks) {
            callback.transferData(from, to, merge);
        }
    });

    public static final Event<PreSplit> PRE_SPLIT = EventFactory.createArrayBacked(PreSplit.class, (callbacks) -> (whole) -> {
        for (PreSplit callback : callbacks) {
            if (!callback.canSplit(whole)) {
                return false;
            }
        }
        return true;
    });

    /**
     * Fired after a player has split into a soul and a shell
     */
    public static final Event<Split> PLAYER_SPLIT = EventFactory.createArrayBacked(Split.class, (callbacks) -> (whole, soul, playerShell) -> {
        for (Split callback : callbacks) {
            callback.onPlayerSplit(whole, soul, playerShell);
        }
    });

    /**
     * Fired before a soul merges with a shell
     */
    public static final Event<PreMerge> PRE_MERGE = EventFactory.createArrayBacked(PreMerge.class, (callbacks) -> (player, playerShell, shellProfile) -> {
        for (PreMerge callback : callbacks) {
            if (!callback.canMerge(player, playerShell, shellProfile)) {
                return false;
            }
        }
        return true;
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
    public interface DataTransfer {
        void transferData(ServerPlayerEntity from, ServerPlayerEntity to, boolean merge);
    }

    @FunctionalInterface
    public interface PreSplit {
        /**
         * Fired before a player splits
         * @param whole       the player before it starts splitting
         */
        boolean canSplit(PlayerEntity whole);
    }

    @FunctionalInterface
    public interface Split {
        /**
         * Fired when a shell is being created.
         *
         * <p>If the shell is created as part of regular gameplay or using the "/pandemonium shell split"
         * command, {@code whole} and {@code soul} will be 2 different entities ({@code whole != soul},
         * with the former being now removed from the world, and with the latter being now added to the world.
         *
         * @param whole       the player before it started splitting, now removed from the world
         * @param soul        the respawned player, now added to the world
         * @param playerShell the shell being left behind, added to the world on the next tick
         */
        void onPlayerSplit(ServerPlayerEntity whole, ServerPlayerEntity soul, ServerPlayerEntity playerShell);
    }

    @FunctionalInterface
    public interface PreMerge {
        /**
         * @param player       the player merging with the shell
         * @param playerShell  the shell being merged with
         * @param shellProfile the profile of the player which created this shell
         * @return {@code true} if the merge can happen, {@code false} otherwise
         */
        boolean canMerge(PlayerEntity player, PlayerEntity playerShell, @Nullable GameProfile shellProfile);
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
