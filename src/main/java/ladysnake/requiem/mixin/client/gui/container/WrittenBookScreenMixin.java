package ladysnake.requiem.mixin.client.gui.container;

import ladysnake.requiem.client.gui.EditOpusScreen;
import ladysnake.requiem.common.item.OpusDemoniumItem;
import ladysnake.requiem.common.item.WrittenOpusItem;
import net.minecraft.client.gui.WrittenBookScreen;
import net.minecraft.client.gui.container.LecternScreen;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WrittenBookScreen.class)
public class WrittenBookScreenMixin {

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureManager;bindTexture(Lnet/minecraft/util/Identifier;)V"))
    private Identifier switchBookTexture(Identifier texture) {
        WrittenBookScreen self = (WrittenBookScreen) (Object) this;
        //noinspection ConstantConditions
        if (self instanceof LecternScreen) {
            Item book = ((LecternScreen)self).method_17573().getBookItem().getItem();
            if (book instanceof WrittenOpusItem || book instanceof OpusDemoniumItem) {
                return EditOpusScreen.BOOK_TEXTURE;
            }
        }
        return texture;
    }
}
