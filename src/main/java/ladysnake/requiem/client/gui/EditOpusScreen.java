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
package ladysnake.requiem.client.gui;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.item.OpusDemoniumItem;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.mixin.client.opus.EditBookScreenAccessor;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EditOpusScreen extends BookEditScreen {
    public static final Identifier BOOK_TEXTURE = Requiem.id("textures/gui/opus_daemonium.png");
    public static final Identifier XP_COST_TEXTURE = Requiem.id("textures/gui/required_xp_5.png");
    public static final int REQUIRED_XP = OpusDemoniumItem.REQUIRED_CONVERSION_XP;

    private final Map<String, RemnantType> incantations;
    private boolean validSentence;

    public EditOpusScreen(PlayerEntity player, ItemStack book, Hand hand) {
        super(player, book, hand);
        this.incantations = new HashMap<>();
        for (RemnantType type : RequiemRegistries.REMNANT_STATES) {
            String incantation = type.getConversionBookSentence();
            if (incantation != null) {
                // User locale can theoretically cause incompatibilities, but english locale does not support accents
                this.incantations.put(I18n.translate(incantation).toLowerCase(Locale.getDefault()), type);
            }
        }
    }

    protected <T extends AbstractButtonWidget> void removeButton(T button) {
        this.buttons.remove(button);
        this.children.remove(button);
    }

    @Override
    protected void init() {
        super.init();
        EditBookScreenAccessor access = ((EditBookScreenAccessor)this);
        this.removeButton(access.getNextPageButton());
        this.removeButton(access.getPreviousPageButton());
        this.removeButton(access.getSignButton());
        this.removeButton(access.getFinalizeButton());
        this.removeButton(access.getCancelButton());
        access.setSignButton(this.addButton(new ButtonWidget(
                this.width / 2 - 100, 196, 98, 20,
                new TranslatableText("book.signButton"),
                (widget) -> this.finalizeOpus(true)))
        );
        this.removeButton(access.getDoneButton());
        access.setDoneButton(this.addButton(new ButtonWidget(
                this.width / 2 + 2, 196, 98, 20,
                new TranslatableText("gui.done"),
                (widget) -> this.finalizeOpus(false)))
        );
        checkMagicSentence();
    }

    private void finalizeOpus(boolean sign) {
        Objects.requireNonNull(this.client).openScreen(null);
        if (((EditBookScreenAccessor)this).isDirty() || sign) {
            String firstPage = getFirstPage();   // it's the only one we accept
            Hand hand = ((EditBookScreenAccessor) this).getHand();
            RequiemNetworking.sendToServer(RequiemNetworking.createOpusUpdateBuffer(firstPage, sign, this.incantations.get(firstPage.toLowerCase(Locale.getDefault())), hand));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        assert this.client != null;
        assert this.client.player != null;
        this.renderBackground(matrices);
        this.setFocused(null);
        this.client.getTextureManager().bindTexture(BOOK_TEXTURE);
        int bgX = (this.width - 192) / 2;
        this.drawTexture(matrices, bgX, 2, 0, 0, 192, 192);
        String page = this.getFirstPage();
        this.textRenderer.drawTrimmed(new LiteralText(page), bgX + 36, 32, 114, 0);

        for (AbstractButtonWidget button : this.buttons) {
            button.render(matrices, mouseX, mouseY, tickDelta);
        }

        ButtonWidget signButton = ((EditBookScreenAccessor) this).getSignButton();
        int x = signButton.x + signButton.getWidth() - 22;
        int y = signButton.y - 13;
        this.client.getTextureManager().bindTexture(XP_COST_TEXTURE);
        if (this.validSentence) {
            if (this.client.player.experienceLevel < REQUIRED_XP && !this.client.player.abilities.creativeMode) {
                drawTexture(matrices, x + 1, y + 15, 0, 16, 16, 16, 32, 32);
            } else {
                drawTexture(matrices , x + 1, y + 15, 0, 0, 16, 16, 32, 32);
            }
        }
    }

    private String getFirstPage() {
        return ((EditBookScreenAccessor) this).getPages().get(0);
    }

    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (super.keyPressed(int_1, int_2, int_3)) {
            checkMagicSentence();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (super.charTyped(char_1, int_1)) {
            checkMagicSentence();
            return true;
        }
        return false;
    }

    private void checkMagicSentence() {
        assert this.client != null;
        assert this.client.player != null;
        // Strings are lowercase in the map to make the check case insensitive
        this.validSentence = this.incantations.containsKey(this.getFirstPage().toLowerCase(Locale.getDefault()));
        ((EditBookScreenAccessor) this).getSignButton().active = this.validSentence && (this.client.player.experienceLevel >= REQUIRED_XP || this.client.player.isCreative());
    }
}
