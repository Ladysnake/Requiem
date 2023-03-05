/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.mixin.client.inventory;

import ladysnake.requiem.Requiem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {
    private static final ThreadLocal<Boolean> REQUIEM$REENTRANT = ThreadLocal.withInitial(() -> false);
    @Shadow
    protected MinecraftClient client;
    @Shadow
    protected ToggleButtonWidget toggleCraftableButton;
    @Shadow
    private TextFieldWidget searchField;
    @Shadow
    @Final
    private List<RecipeGroupButtonWidget> tabButtons;
    @Shadow
    @Final
    private RecipeBookResults recipesArea;

    @Shadow
    public abstract void render(MatrixStack matrices, int mouseX, int mouseY, float delta);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void requiem$tryDebugNpe(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!REQUIEM$REENTRANT.get()) {
            REQUIEM$REENTRANT.set(true);
            try {
                this.render(matrices, mouseX, mouseY, delta);
                ci.cancel();
            } catch (Exception e) {
                Requiem.LOGGER.error("Caught exception while rendering recipe book widget", e);
                Requiem.LOGGER.error(
                    "client: {}\n" +
                        "searchField: {}\n" +
                        "searchField.getText(): {}\n" +
                        "tabButtons: {}\n" +
                        "toggleCraftableButton: {}\n" +
                        "recipesArea: {}\n",
                    this.client, this.searchField, this.searchField == null ? null : this.searchField.getText(), this.tabButtons, this.toggleCraftableButton, this.recipesArea);
                throw e;
            } finally {
                REQUIEM$REENTRANT.set(false);
            }
        }
    }
}
