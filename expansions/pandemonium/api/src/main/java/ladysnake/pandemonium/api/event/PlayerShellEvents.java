/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerShellEvents {
    /**
     * Fired after a player has split into a soul and a shell
     */
    public static final Event<Split> PLAYER_SPLIT = EventFactory.createArrayBacked(Split.class, (callbacks) -> (whole, soul, playerShell, playerData) -> {
        for (Split callback : callbacks) {
            callback.onPlayerSplit(whole, soul, playerShell, playerData);
        }
    });

    /**
     * Fired after a soul has merged with a shell
     */
    public static final Event<Merge> PLAYER_MERGED = EventFactory.createArrayBacked(Merge.class, (callbacks) -> (player, playerShell, shellProfile, playerData) -> {
        for (Merge callback : callbacks) {
            callback.onPlayerMerge(player, playerShell, shellProfile, playerData);
        }
    });

    @FunctionalInterface
    public interface Split {
        /**
         * @param whole       the player before it started splitting, now removed from the world
         * @param soul        the respawned player
         * @param playerShell the shell being left behind
         * @param playerData  the additional player data stored on the shell, can be modified by listeners
         */
        void onPlayerSplit(ServerPlayerEntity whole, ServerPlayerEntity soul, MobEntity playerShell, CompoundTag playerData);
    }

    @FunctionalInterface
    public interface Merge {
        /**
         * @param player       the player merging with the shell
         * @param playerShell  the shell being merged with
         * @param shellProfile the profile of the player which created this shell
         * @param playerData   the additional player data stored on the shell
         */
        void onPlayerMerge(ServerPlayerEntity player, MobEntity playerShell, GameProfile shellProfile, CompoundTag playerData);
    }
}
