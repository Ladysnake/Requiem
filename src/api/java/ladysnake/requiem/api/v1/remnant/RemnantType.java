/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
 */
package ladysnake.requiem.api.v1.remnant;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * A {@link RemnantType} contains information about a specific kind
 * of {@link RemnantState}.
 *
 * @see net.minecraft.entity.EntityType
 * @see net.minecraft.block.entity.BlockEntityType
 */
public interface RemnantType {
    /**
     * Creates a new remnant state of this type, for the given player
     */
    RemnantState create(PlayerEntity player);

    /**
     * Gets the unlocalized string corresponding to the sentence the player
     * should input in an Opus Daemonium to change it into a conversion item
     *
     * @return a localization key
     */
    @Nullable
    default String getConversionBookSentence() {
        return null;
    }

    /**
     * Gets the conversion item making a player into this remnant type.
     * The returned itemstack should always be a new instance.
     *
     * @return a new stack of the conversion item for this remnant type
     * @param player the player triggering the conversion, if any
     */
    default ItemStack getConversionBook(@Nullable PlayerEntity player) {
        return ItemStack.EMPTY; // The empty stack is a singleton
    }
}
