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

import com.google.common.collect.ImmutableList;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.client.ZaWorldFx;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class CutsceneDialogueScreen extends Screen {
    private final CutsceneDialogue dialogue;
    private int selectedChoice;

    public CutsceneDialogueScreen(Component title, CutsceneDialogue dialogue) {
        super(title);
        this.dialogue = dialogue;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        confirmCurrentChoice();
        return true;
    }

    private void confirmCurrentChoice() {
        if (this.dialogue.choose(this.dialogue.getCurrentChoices().get(selectedChoice))) {
            Objects.requireNonNull(this.minecraft).openScreen(null);
            ((RequiemPlayer)this.minecraft.player).getDialogueTracker().endDialogue();
            ((RequiemPlayer) this.minecraft.player).getDeathSuspender().setLifeTransient(false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int int_2, int int_3) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            confirmCurrentChoice();
            return true;
        }
        return super.keyPressed(keyCode, int_2, int_3);
    }

    @Override
    public boolean changeFocus(boolean shiftPressed) {
        this.selectedChoice = Math.floorMod(this.selectedChoice + (shiftPressed ? 1 : -1), this.dialogue.getCurrentChoices().size());
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        this.selectedChoice = Math.floorMod((int) (this.selectedChoice - scrollAmount), this.dialogue.getCurrentChoices().size());
        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        if (!ZaWorldFx.INSTANCE.hasFinishedAnimation()) {
            return;
        }
        this.renderBackground();
        int y = 40;
        String title = I18n.translate(this.dialogue.getCurrentText());
        final int width = 200;
        this.font.drawStringBounded(title, 10, y, width, 0xFFFFFF);
        y += this.font.getStringBoundedHeight(title, width) + 20;
        ImmutableList<String> choices = this.dialogue.getCurrentChoices();
        for (int i = 0; i < choices.size(); i++) {
            String choice = I18n.translate(choices.get(i));
            this.font.drawStringBounded(choice, 10, y, width, i == selectedChoice ? 0xE0E044 : 0xA0A0A0);
            y += this.font.getStringBoundedHeight(choice, width) + 5;
        }
        String tip = I18n.translate("requiem:dialogue.instructions");
        this.font.draw(tip, (this.width - font.getStringWidth(tip)) * 0.5f, this.height - 30, 0x808080);
        super.render(mouseX, mouseY, tickDelta);
    }
}
