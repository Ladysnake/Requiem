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
package ladysnake.requiem.common.remnant;

import com.google.common.base.Suppliers;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleRemnantType implements RemnantType {
    protected final Function<PlayerEntity, RemnantState> factory;
    protected final String conversionSentence;
    protected final Supplier<Item> conversionBook;
    private final boolean remnant;
    private final Supplier<Text> name = Suppliers.memoize(() -> Text.translatable(
        "requiem:remnant_type." + RemnantTypes.getId(this).toString().replace(':', '.') + ".name"
    ));

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
    public boolean isDemon() {
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

    @Override
    public Text getName() {
        return this.name.get();
    }
}
