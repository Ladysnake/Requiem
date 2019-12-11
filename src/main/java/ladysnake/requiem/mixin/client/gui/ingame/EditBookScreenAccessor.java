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

import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.PageTurnWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BookEditScreen.class)
public interface EditBookScreenAccessor {
    @Accessor
    PageTurnWidget getPreviousPageButton();
    @Accessor
    PageTurnWidget getNextPageButton();
    @Accessor
    ButtonWidget getDoneButton();
    @Accessor
    ButtonWidget getSignButton();
    @Accessor
    void setSignButton(ButtonWidget button);
    @Accessor
    void setDoneButton(ButtonWidget button);
    @Accessor
    boolean isDirty();
    @Accessor
    Hand getHand();
    @Accessor
    List<String> getPages();
    @Accessor
    ButtonWidget getFinalizeButton();
    @Accessor
    ButtonWidget getCancelButton();
    @Accessor
    int getCursorIndex();
    @Invoker
    void invokeDrawHighlight(String page);
    @Accessor
    int getTickCounter();
}
