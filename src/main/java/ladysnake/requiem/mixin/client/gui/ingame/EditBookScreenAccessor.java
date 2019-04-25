package ladysnake.requiem.mixin.client.gui.ingame;

import net.minecraft.client.gui.ingame.EditBookScreen;
import net.minecraft.client.gui.widget.BookPageButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EditBookScreen.class)
public interface EditBookScreenAccessor {
    @Accessor
    BookPageButtonWidget getButtonPreviousPage();
    @Accessor
    BookPageButtonWidget getButtonNextPage();
    @Accessor
    ButtonWidget getButtonDone();
    @Accessor
    ButtonWidget getButtonSign();
    @Accessor
    void setButtonSign(ButtonWidget button);
    @Accessor
    void setButtonDone(ButtonWidget button);
    @Accessor
    boolean isDirty();
    @Accessor
    Hand getHand();
    @Accessor
    List<String> getPages();
    @Accessor
    ButtonWidget getButtonFinalize();
    @Accessor
    ButtonWidget getButtonCancel();

}
