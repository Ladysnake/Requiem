package ladysnake.requiem.client.gui;

import com.google.common.collect.ImmutableList;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TextComponent;

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
        if (this.dialogue.choose(this.dialogue.getCurrentChoices().get(selectedChoice))) {
            Objects.requireNonNull(this.minecraft).openScreen(null);
            ((RequiemPlayer)this.minecraft.player).getDialogueTracker().endDialogue();
        }
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
        final int width = 100;
        this.font.drawStringBounded(title, 10, y, width, 0x0);
        y += this.font.getStringBoundedHeight(title, width);
        ImmutableList<String> choices = this.dialogue.getCurrentChoices();
        for (int i = 0; i < choices.size(); i++) {
            String choice = choices.get(i);
            this.font.drawStringBounded(choice, 10, y, width, i == selectedChoice ? 0xFF00FF : 0x0);
            y += this.font.getStringBoundedHeight(choice, width);
        }
        super.render(mouseX, mouseY, tickDelta);
    }
}
