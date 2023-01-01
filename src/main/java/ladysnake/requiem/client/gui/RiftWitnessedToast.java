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
package ladysnake.requiem.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.common.block.RequiemBlocks;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class RiftWitnessedToast implements Toast {
    private static final MutableText RIFT_WITNESSED_TEXT = Text.translatable("requiem:toast.rift_witnessed");
    private static final ItemStack RIFT_BLOCK = new ItemStack(RequiemBlocks.RIFT_RUNE);
    private final Text riftName;

    public RiftWitnessedToast(Text riftName) {
        this.riftName = riftName;
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());
        List<OrderedText> lines = manager.getGame().textRenderer.wrapLines(this.riftName, 125);
        int headerColor = 0xff88ff;
        switch (lines.size()) {
            case 0 -> manager.getGame().textRenderer.draw(matrices, RIFT_WITNESSED_TEXT, 30.0F, 11.0F, headerColor | 0xFF000000);
            case 1 -> {
                manager.getGame().textRenderer.draw(matrices, RIFT_WITNESSED_TEXT, 30.0F, 7.0F, headerColor | 0xFF000000);
                manager.getGame().textRenderer.draw(matrices, lines.get(0), 30.0F, 18.0F, -1);
            }
            default -> {
                if (startTime < 1500L) {
                    int k = MathHelper.floor(MathHelper.clamp((float)(1500L - startTime) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 0x4000000;
                    manager.getGame().textRenderer.draw(matrices, RIFT_WITNESSED_TEXT, 30.0F, 11.0F, headerColor | k);
                } else {
                    int k = MathHelper.floor(MathHelper.clamp((float)(startTime - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 0x4000000;
                    int y = this.getHeight() / 2 - lines.size() * 9 / 2;

                    for(OrderedText orderedText : lines) {
                        manager.getGame().textRenderer.draw(matrices, orderedText, 30.0F, (float)y, 0xffffff | k);
                        y += 9;
                    }
                }
            }
        }

        manager.getGame().getItemRenderer().renderInGui(RIFT_BLOCK, 8, 8);
        return startTime >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
