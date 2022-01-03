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
package ladysnake.requiem.api.v1.remnant;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.apiguardian.api.API;

import javax.annotation.Nullable;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

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
     * Controls behaviours external to the remnant state itself, such
     * as demon rendering effects.
     *
     * @return true if players of this type can be considered demons
     */
    @API(status = EXPERIMENTAL)
    boolean isDemon();

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

    Text getName();
}
