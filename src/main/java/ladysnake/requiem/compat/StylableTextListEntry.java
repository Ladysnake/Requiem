/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.compat;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.mixin.client.access.ScreenAccessor;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public class StylableTextListEntry extends TextListEntry {
    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    private final Text text;
    private final int color;
    private int savedWidth = -1, savedX = -1, savedY = -1;
    private List<OrderedText> wrappedLines;

    @SuppressWarnings({"deprecation", "UnstableApiUsage"})
    public StylableTextListEntry(Text fieldName, Text text, int color, Supplier<Optional<Text[]>> tooltipSupplier) {
        super(fieldName, text, color, tooltipSupplier);
        this.text = text;
        this.color = color;
        this.wrappedLines = Collections.emptyList();
    }

    @Override
    public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        if (this.savedWidth != entryWidth || this.savedX != x || this.savedY != y) {
            this.wrappedLines = this.textRenderer.wrapLines(this.text, entryWidth);
            this.savedWidth = entryWidth;
            this.savedX = x;
            this.savedY = y;
        }
        int yy = y + 4;
        for (OrderedText line : this.wrappedLines) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, line, x, yy, color);
            yy += MinecraftClient.getInstance().textRenderer.fontHeight + 3;
        }

        Style style = this.getTextAt(mouseX, mouseY);
        Screen configScreen = this.getConfigScreen();

        if (style != null && configScreen != null) {
            ((ScreenAccessor) configScreen).invokeRenderTextHoverEffect(matrices, style, mouseX, mouseY);
        }
    }

    @Override
    public int getItemHeight() {
        if (savedWidth == -1) return 12;
        int lineCount = this.wrappedLines.size();
        return lineCount == 0 ? 0 : 15 + lineCount * 12;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Style style = this.getTextAt(mouseX, mouseY);
            if (style != null) {
                ClickEvent clickEvent = style.getClickEvent();
                if (clickEvent != null) {
                    URI uri;
                    if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
                        try {
                            uri = new URI(clickEvent.getValue());
                            String string = uri.getScheme();
                            if (string == null) {
                                throw new URISyntaxException(clickEvent.getValue(), "Missing protocol");
                            }

                            if (!(string.equalsIgnoreCase("http") || string.equalsIgnoreCase("https"))) {
                                throw new URISyntaxException(clickEvent.getValue(), "Unsupported protocol: " + string.toLowerCase(Locale.ROOT));
                            }

                            MinecraftClient.getInstance().openScreen(new ConfirmChatLinkScreen(openInBrowser -> {
                                if (openInBrowser) {
                                    Util.getOperatingSystem().open(uri);
                                }

                                MinecraftClient.getInstance().openScreen(this.getConfigScreen());
                            }, clickEvent.getValue(), true));
                        } catch (URISyntaxException var5) {
                            Requiem.LOGGER.error("Can't open url for {}", clickEvent, var5);
                        }
                    }
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Nullable
    private Style getTextAt(double x, double y) {
        int lineCount = this.wrappedLines.size();

        if (lineCount > 0) {
            int textX = MathHelper.floor(x - this.savedX);
            int textY = MathHelper.floor(y - 4 - this.savedY);
            if (textX >= 0 && textY >= 0 && textX <= this.savedWidth && textY < 12 * lineCount + lineCount) {
                int line = textY / 12;
                if (line < this.wrappedLines.size()) {
                    OrderedText orderedText = this.wrappedLines.get(line);
                    return this.textRenderer.getTextHandler().getStyleAt(orderedText, textX);
                }
            }
        }
        return null;
    }

}
