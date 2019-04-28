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
package ladysnake.requiem.mixin.client.gui.ingame;

import net.minecraft.client.gui.ingame.EditBookScreen;
import net.minecraft.client.gui.widget.BookPageButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

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
    @Accessor
    int getCursorIndex();
    @Invoker
    void invokeDrawHighlight(String page);
    @Accessor
    int getTickCounter();
}
