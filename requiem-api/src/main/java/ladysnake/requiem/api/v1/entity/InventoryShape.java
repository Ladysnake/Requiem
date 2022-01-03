/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public enum InventoryShape {
    NORMAL(null, 26, 8, 49, 70, 0, 0) {
        @Override
        public boolean isAltShape() {
            return false;
        }

        @Override
        public Identifier swapBackground(Identifier baseBackground) {
            return baseBackground;
        }

        @Override
        public void setupEntityCrop(int screenX, int screenY) {
            // NO-OP
        }

        @Override
        public void tearDownEntityCrop() {
            // NO-OP
        }
    },
    ALT(background("alt_inventory.png"), 16, 83, 144, 70, 40, 75),
    ALT_SMALL(background("alt_inventory_small.png"), 26, 8, 49, 70, 0, 0),
    ALT_LARGE(background("alt_inventory_large.png"), 16, 18, 144, 135, 40, 75);

    private final int entityX;
    private final int entityY;
    private final int entityWidth;
    private final int entityHeight;
    private final int entityShiftX;
    private final int entityShiftY;

    private static Identifier background(String name) {
        return new Identifier("requiem", "textures/gui/" + name);
    }

    private final @Nullable Identifier background;

    InventoryShape(@Nullable Identifier background, int entityX, int entityY, int entityWidth, int entityHeight, int entityShiftX, int entityShiftY) {
        this.background = background;
        this.entityX = entityX;
        this.entityY = entityY;
        this.entityWidth = entityWidth;
        this.entityHeight = entityHeight;
        this.entityShiftX = entityShiftX;
        this.entityShiftY = entityShiftY;
    }

    public Identifier swapBackground(Identifier baseBackground) {
        assert this.background != null;
        return this.background;
    }

    public boolean isAltShape() {
        return true;
    }

    public boolean hidesMainInventory() {
        return this == ALT || this == ALT_LARGE;
    }

    /**
     * Sets up cropping before rendering an entity in the inventory
     */
    public void setupEntityCrop(int screenX, int screenY) {
        Window window = MinecraftClient.getInstance().getWindow();
        int scissorX = unscale(screenX + entityX, window.getScaledWidth(), window.getWidth());
        // mc screen coordinates start at the top, but scissor coordinates start at the bottom
        int scissorY = unscale(window.getScaledHeight() - (screenY + entityY + entityHeight), window.getScaledHeight(), window.getHeight());
        int scissorWidth = unscale(entityWidth, window.getScaledWidth(), window.getWidth());
        int scissorHeight = unscale(entityHeight, window.getScaledHeight(), window.getHeight());
        RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    private static int unscale(int scaled, float windowScaled, int windowReal) {
        return (int) ((scaled / windowScaled) * windowReal);
    }

    public void tearDownEntityCrop() {
        RenderSystem.disableScissor();
    }

    public float shiftEntityX(float x) {
        return x + this.entityShiftX;
    }

    public float shiftEntityY(float y) {
        return y + this.entityShiftY;
    }
}
