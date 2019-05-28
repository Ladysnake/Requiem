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
package ladysnake.requiem.common.remnant;

import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleRemnantType implements RemnantType {
    protected final Function<PlayerEntity, RemnantState> factory;
    protected final String conversionSentence;
    protected final Supplier<Item> conversionBook;
    private boolean remnant;

    public SimpleRemnantType(Function<PlayerEntity, RemnantState> factory, boolean remnant, String conversionSentence, Supplier<Item> conversionBook) {
        this.factory = factory;
        this.remnant = remnant;
        this.conversionSentence = conversionSentence;
        this.conversionBook = conversionBook;
    }

    @Override
    public RemnantState create(PlayerEntity player) {
        return this.factory.apply(player);
    }

    @Override
    public boolean isRemnant() {
        return remnant;
    }

    @Nullable
    @Override
    public String getConversionBookSentence() {
        return conversionSentence;
    }

    @Override
    public ItemStack getConversionBook(@Nullable PlayerEntity player) {
        return new ItemStack(this.conversionBook.get());
    }
}
