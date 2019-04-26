package ladysnake.requiem.mixin.client.gui.container;

import net.minecraft.client.gui.WrittenBookScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WrittenBookScreen.Contents.class)
public interface WrittenBookScreenContentsMixin {
/* TODO enable when mixin supports injection into interface static methods
    @Inject(method = "create", at = @At("TAIL"), cancellable = true)
    static void handleOpiDaemonium(ItemStack book, CallbackInfoReturnable<WrittenBookScreen.Contents> cir) {
        Item item = book.getItem();
        if (item instanceof OpusDemoniumItem) {
            cir.setReturnValue(new WrittenBookScreen.WritableBookContents(book));
        } else if (item instanceof WrittenOpusItem) {
            cir.setReturnValue(new WrittenOpusContents((WrittenOpusItem) item));
        }
    }
*/
}
