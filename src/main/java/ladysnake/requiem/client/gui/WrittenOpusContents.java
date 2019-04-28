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
package ladysnake.requiem.client.gui;

import ladysnake.requiem.common.item.WrittenOpusItem;
import net.minecraft.client.gui.WrittenBookScreen;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TranslatableTextComponent;

public class WrittenOpusContents implements WrittenBookScreen.Contents {
    private final TextComponent magicSentence;

    public WrittenOpusContents(WrittenOpusItem book) {
        this.magicSentence = new TranslatableTextComponent(book.getRemnantType().getConversionBookSentence())
                .applyFormat(book.getTooltipColor());
    }

    @Override
    public int getLineCount() {
        return 1;
    }

    @Override
    public TextComponent getLine(int lineNo) {
        if (lineNo == 0) {
            return this.magicSentence;
        }
        return null;
    }
}
