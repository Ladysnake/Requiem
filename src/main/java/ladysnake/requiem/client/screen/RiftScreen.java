/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.screen.RiftScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RiftScreen extends HandledScreen<RiftScreenHandler> {
    private static final Identifier RIFT_ICONS = Requiem.id("textures/gui/soul_rift.png");

    private @Nullable Matrix4f projectionViewMatrix;
    private @Nullable BlockPos currentMouseOver;
    private int selectionIndex = 0;
    private int overlappingSelections = 1;

    public RiftScreen(RiftScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        // Actually nothing
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        if (this.projectionViewMatrix != null) {
            int obeliskV = 0;
            int sourceObeliskV = 16;
            int selectedObeliskV = 32;
            int iconSize = 16;
            int iconHalfSize = iconSize / 2;
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            this.currentMouseOver = null;
            List<BlockPos> selected = new ArrayList<>();
            RenderSystem.setShaderTexture(0, RIFT_ICONS);

            for (BlockPos pos : this.handler.getObeliskPositions()) {
                Vec3f projected = worldToScreenSpace(this.projectionViewMatrix, pos);
                // Only render the obelisks that are in front of the player
                if (projected.getZ() > 0) {
                    int v;
                    if (pos.equals(this.getScreenHandler().getSource())) {
                        v = sourceObeliskV;
                    } else if (projected.getX() > (centerX - iconHalfSize) && projected.getX() < (centerX + iconHalfSize) && projected.getY() > (centerY - iconHalfSize) && projected.getY() < (centerY + iconHalfSize)) {
                        v = selectedObeliskV;
                        selected.add(pos);
                    } else {
                        v = obeliskV;
                    }
                    drawTexture(matrices, Math.round(projected.getX()) - iconHalfSize, Math.round(projected.getY()) - iconHalfSize, this.getZOffset(), 0, v, iconSize, iconSize, 48, 16);
                }
            }

            if (!selected.isEmpty()) {
                this.overlappingSelections = selected.size();
                int selectedIndex = this.getSelectionIndex();
                List<Text> lines = new ArrayList<>(selected.size());

                for (int i = 0; i < selected.size(); i++) {
                    BlockPos pos = selected.get(i);
                    Formatting formatting;

                    if (i == selectedIndex) {
                        this.currentMouseOver = pos;
                        formatting = Formatting.LIGHT_PURPLE;
                    } else {
                        formatting = Formatting.GRAY;
                    }

                    lines.add(new LiteralText(pos.toShortString()).formatted(formatting));
                }

                this.renderTooltip(matrices, lines, centerX, centerY);
            } else {
                this.overlappingSelections = 1;
            }
        }

        this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 0xA0A0A0);
    }

    private int getSelectionIndex() {
        return MathHelper.clamp(this.selectionIndex, 0, overlappingSelections - 1);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private Vec3f worldToScreenSpace(Matrix4f projectionViewMatrix, BlockPos worldPos) {
        Vec3d cameraPos = Objects.requireNonNull(client).gameRenderer.getCamera().getPos();
        Vector4f clipSpacePos = new Vector4f(
            (float) (worldPos.getX() + 0.5F - cameraPos.getX()),
            (float) (worldPos.getY() + 0.5F - cameraPos.getY()),
            (float) (worldPos.getZ() + 0.5F - cameraPos.getZ()),
            1.0F
        );
        clipSpacePos.transform(projectionViewMatrix);

        // If W is strictly negative, the obelisk is behind us and we do not want to display it
        // If W is 0, we cannot perform the division
        if (clipSpacePos.getW() <= 0) {
            return Vec3f.ZERO;
        }

        clipSpacePos.normalizeProjectiveCoordinates();

        return new Vec3f(
            ((clipSpacePos.getX() + 1f) / 2f) * this.width,
            ((-clipSpacePos.getY() + 1f) / 2f) * this.height,   // screen coordinates origin are at top-left
            clipSpacePos.getZ() // we only use the depth to know if a point is behind another point
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.currentMouseOver != null) {
            RequiemNetworking.sendRiftUseMessage(this.currentMouseOver);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.selectionIndex = Math.floorMod((int) (this.getSelectionIndex() - amount), this.overlappingSelections);
        return true;
    }

    @Override
    protected void init() {
        super.init();
        this.x = 0;
        this.y = 0;
        this.backgroundWidth = this.width;
        this.backgroundHeight = this.height;
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    public void updateMatrices(MatrixStack modelViewStack, Matrix4f projectionMatrix) {
        this.projectionViewMatrix = projectionMatrix.copy();
        this.projectionViewMatrix.multiply(modelViewStack.peek().getModel());
    }
}
