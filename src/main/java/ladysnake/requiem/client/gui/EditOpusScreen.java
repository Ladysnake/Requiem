package ladysnake.requiem.client.gui;

import ladysnake.requiem.common.item.OpusDemoniumItem;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.mixin.client.gui.ingame.EditBookScreenAccessor;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ingame.EditBookScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.Objects;

public class EditOpusScreen extends EditBookScreen {
    public EditOpusScreen(PlayerEntity player, ItemStack book, Hand hand) {
        super(player, book, hand);
    }

    protected <T extends AbstractButtonWidget> void removeButton(T button) {
        this.buttons.remove(button);
        this.children.remove(button);
    }

    @Override
    protected void init() {
        super.init();
        EditBookScreenAccessor access = ((EditBookScreenAccessor)this);
        this.removeButton(access.getButtonNextPage());
        this.removeButton(access.getButtonPreviousPage());
        this.removeButton(access.getButtonSign());
        this.removeButton(access.getButtonFinalize());
        this.removeButton(access.getButtonCancel());
        access.setButtonSign(this.addButton(new ButtonWidget(
                this.width / 2 - 100, 196, 98, 20,
                I18n.translate("book.signButton"),
                (widget) -> this.finalizeOpus(true)))
        );
        this.removeButton(access.getButtonDone());
        access.setButtonDone(this.addButton(new ButtonWidget(
                this.width / 2 + 2, 196, 98, 20,
                I18n.translate("gui.done"),
                (widget) -> this.finalizeOpus(false)))
        );
        checkMagicSentence();
    }

    private void finalizeOpus(boolean sign) {
        Objects.requireNonNull(this.minecraft).openScreen(null);
        if (((EditBookScreenAccessor)this).isDirty()) {
            String firstPage = getFirstPage();   // it's the only one we accept
            Hand hand = ((EditBookScreenAccessor) this).getHand();
            RequiemNetworking.sendToServer(RequiemNetworking.createOpusUpdateBuffer(firstPage, sign, hand));
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

    private void checkMagicSentence() {
        String firstPage = this.getFirstPage();
        ((EditBookScreenAccessor) this).getButtonSign().active = firstPage.equals(OpusDemoniumItem.CURE_SENTENCE)
                || firstPage.equals(OpusDemoniumItem.CURSE_SENTENCE);
    }
}
