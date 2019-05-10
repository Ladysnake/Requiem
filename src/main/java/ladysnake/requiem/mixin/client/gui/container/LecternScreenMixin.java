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

import ladysnake.requiem.client.gui.WrittenOpusContents;
import ladysnake.requiem.common.item.OpusDemoniumItem;
import ladysnake.requiem.common.item.WrittenOpusItem;
import net.minecraft.client.gui.WrittenBookScreen;
import net.minecraft.client.gui.container.LecternScreen;
import net.minecraft.container.LecternContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternScreen.class)
public abstract class LecternScreenMixin extends WrittenBookScreen {
    @Shadow @Final private LecternContainer lecternContainer;

    @Inject(method = "updatePageProvider", at = @At("HEAD"), cancellable = true)
    private void updatePageProvider(CallbackInfo ci) {
        ItemStack book = this.lecternContainer.getBookItem();
        Item bookItem = book.getItem();
        if (bookItem instanceof OpusDemoniumItem) {
            this.setPageProvider(new WrittenBookScreen.WritableBookContents(book));
            ci.cancel();
        } else if (bookItem instanceof WrittenOpusItem) {
            this.setPageProvider(new WrittenOpusContents((WrittenOpusItem) bookItem));
            ci.cancel();
        }
    }
}
