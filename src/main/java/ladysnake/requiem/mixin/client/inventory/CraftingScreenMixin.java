/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends HandledScreen<CraftingScreenHandler> {
    @Unique
    private TexturedButtonWidget supercrafterButton;

    public CraftingScreenMixin(CraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addSupercrafterButton(CallbackInfo ci) {
        MobEntity possessedEntity = PossessionComponent.get(this.client.player).getHost();
        if (possessedEntity != null && possessedEntity.getType().isIn(RequiemEntityTypeTags.SUPERCRAFTERS)) {
            this.supercrafterButton = this.addDrawableChild(new TexturedButtonWidget(
                this.x + 5,
                this.height / 2 - 30,
                20,
                18,
                0,
                0,
                19,
                RequiemClient.CRAFTING_BUTTON_TEXTURE,
                (buttonWidget) -> {
                    assert this.client != null;
                    this.close();
                    this.client.setScreen(new InventoryScreen(this.client.player));
                })
            );
        }
    }

    @Dynamic("Lambda method, implementation of PressAction for the crafting book button")
    @Inject(method = "method_19890", at = @At("RETURN"), remap = false)
    private void repositionCraftingButton(ButtonWidget button, CallbackInfo ci) {
        if (this.supercrafterButton != null) {
            this.supercrafterButton.setPos(this.x + 5, this.height / 2 - 30);
        }
    }
}
