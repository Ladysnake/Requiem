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
