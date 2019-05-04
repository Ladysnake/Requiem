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
