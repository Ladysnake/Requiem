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

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Predicate;

/**
 * @since 2.0.0
 */
public final class ConsumableItemEvents {
    public static final Event<Search> SEARCH = EventFactory.createArrayBacked(Search.class,
        callbacks -> (player, action) -> {
            for (Search callback : callbacks) {
                if (callback.findConsumables(player, action)) {
                    return true;
                }
            }
            return false;
        });

    public static final Event<PostConsumed> POST_CONSUMED = EventFactory.createArrayBacked(PostConsumed.class,
        callbacks -> (player, stack) -> {
            for (PostConsumed callback : callbacks) {
                callback.onItemConsumed(player, stack);
            }
        });

    @FunctionalInterface
    public interface Search {
        /**
         * Finds consumable items in a player's inventory.
         *
         * <p>If the action returns {@code true} for a stack,
         * this method must return {@code true} without submitting other stacks.
         *
         * <p>Example implementation:
         * <pre>{@code
         * SearchConsumableCallback.EVENT.register((player, action) -> {
         *     for (ItemStack stack : MyExternalInventory.get(player)) {
         *         if (action.test(stack)) {
         *             return true;
         *         }
         *     }
         *     return false;
         * }
         * }</pre>
         *
         * @param player the player to search consumables for
         * @param action the action to run for any stack found
         * @return {@code true} if a stack was found and accepted, {@code false} otherwise
         */
        boolean findConsumables(ServerPlayerEntity player, Predicate<ItemStack> action);
    }

    public interface PostConsumed {
        void onItemConsumed(ServerPlayerEntity player, ItemStack stack);
    }
}
