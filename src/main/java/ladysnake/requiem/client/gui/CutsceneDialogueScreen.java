package ladysnake.requiem.client.gui;

import com.google.common.collect.ImmutableList;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class CutsceneDialogueScreen extends Screen {
    private final CutsceneDialogue dialogue;
    private int selectedChoice;

    public CutsceneDialogueScreen(TextComponent title, CutsceneDialogue dialogue) {
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
        this.selectedChoice = Math.floorMod(this.selectedChoice + (shiftPressed ? -1 : 1), this.dialogue.getCurrentChoices().size());
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        this.selectedChoice = Math.floorMod((int) (this.selectedChoice - scrollAmount), this.dialogue.getCurrentChoices().size());
        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
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
        super.render(mouseX, mouseY, tickDelta);
    }
}
