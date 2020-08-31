/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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

import com.google.common.collect.ImmutableList;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.dialogue.ChoiceResult;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.client.ZaWorldFx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

public class CutsceneDialogueScreen extends Screen {
    public static final int MIN_RENDER_Y = 40;
    public static final int TITLE_GAP = 20;
    public static final int CHOICE_GAP = 5;
    public static final int MAX_TEXT_WIDTH = 300;

    private final CutsceneDialogue dialogue;
    private int selectedChoice;
    private boolean hoveringChoice;

    public CutsceneDialogueScreen(Text title, CutsceneDialogue dialogue) {
        super(title);
        this.dialogue = dialogue;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (hoveringChoice) {
            confirmChoice(selectedChoice);
        }
        return true;
    }

    private ChoiceResult confirmChoice(int selectedChoice) {
        assert this.client != null;
        ChoiceResult result = this.dialogue.choose(this.dialogue.getCurrentChoices().get(selectedChoice));
        if (result == ChoiceResult.END_DIALOGUE) {
            this.client.openScreen(null);
            RequiemPlayer player = (RequiemPlayer) this.client.player;
            assert player != null;
            player.getDialogueTracker().endDialogue();
            DeathSuspender.get(this.client.player).setLifeTransient(false);
        } else if (result == ChoiceResult.ASK_CONFIRMATION) {
            ImmutableList<Text> choices = this.dialogue.getCurrentChoices();
            this.client.openScreen(new ConfirmScreen(
                    this::onBigChoiceMade,
                    this.dialogue.getCurrentText(),
                    new LiteralText(""),
                    choices.get(0),
                    choices.get(1)
            ));
        } else {
            this.selectedChoice = 0;
        }
        return result;
    }

    private void onBigChoiceMade(boolean yes) {
        assert client != null;
        if (this.confirmChoice(yes ? 0 : 1) == ChoiceResult.DEFAULT) {
            this.client.openScreen(this);
        }
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        GameOptions options = MinecraftClient.getInstance().options;
        if (key == GLFW.GLFW_KEY_ENTER || options.keyInventory.matchesKey(key, scancode)) {
            confirmChoice(selectedChoice);
            return true;
        }
        boolean tab = GLFW.GLFW_KEY_TAB == key;
        boolean down = options.keyBack.matchesKey(key, scancode);
        boolean shift = (GLFW.GLFW_MOD_SHIFT & modifiers) != 0;
        if (tab || down || options.keyForward.matchesKey(key, scancode)) {
            scrollDialogueChoice(tab && !shift || down ? -1 : 1);
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        this.scrollDialogueChoice(scrollAmount);
        return true;
    }

    private void scrollDialogueChoice(double scrollAmount) {
        this.selectedChoice = Math.floorMod((int) (this.selectedChoice - scrollAmount), this.dialogue.getCurrentChoices().size());
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        ImmutableList<Text> choices = this.dialogue.getCurrentChoices();
        Text title = this.dialogue.getCurrentText();
        int y = MIN_RENDER_Y + this.getTextBoundedHeight(title, MAX_TEXT_WIDTH) + TITLE_GAP;
        for (int i = 0; i < choices.size(); i++) {
            Text choice = choices.get(i);
            int strHeight = this.getTextBoundedHeight(choice, width);
            int strWidth = strHeight == 9 ? this.textRenderer.getWidth(choice) : width;
            if (mouseX < strWidth && mouseY > y && mouseY < y + strHeight) {
                this.selectedChoice = i;
                this.hoveringChoice = true;
                return;
            }
            y += strHeight + CHOICE_GAP;
            this.hoveringChoice = false;
        }
    }

    private int getTextBoundedHeight(Text text, int maxWidth) {
        return 9 * this.textRenderer.wrapLines(text, maxWidth).size();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        assert client != null;
        if (!ZaWorldFx.INSTANCE.hasFinishedAnimation()) {
            return;
        }
        this.renderBackground(matrices);
        int y = MIN_RENDER_Y;
        Text title = this.dialogue.getCurrentText();
        this.textRenderer.drawTrimmed(title, 10, y, MAX_TEXT_WIDTH, 0xFFFFFF);
        y += this.getTextBoundedHeight(title, MAX_TEXT_WIDTH) + TITLE_GAP;
        ImmutableList<Text> choices = this.dialogue.getCurrentChoices();
        for (int i = 0; i < choices.size(); i++) {
            Text choice = choices.get(i);
            int strHeight = this.getTextBoundedHeight(choice, MAX_TEXT_WIDTH);
            this.textRenderer.drawTrimmed(choice, 10, y, MAX_TEXT_WIDTH, i == this.selectedChoice ? 0xE0E044 : 0xA0A0A0);
            y += strHeight + CHOICE_GAP;
        }
        Text tip = new TranslatableText("requiem:dialogue.instructions", client.options.keyForward.getBoundKeyLocalizedText(), client.options.keyBack.getBoundKeyLocalizedText(), client.options.keyInventory.getBoundKeyLocalizedText());
        this.textRenderer.draw(matrices, tip, (this.width - this.textRenderer.getWidth(tip)) * 0.5f, this.height - 30, 0x808080);
        super.render(matrices, mouseX, mouseY, tickDelta);
    }
}
