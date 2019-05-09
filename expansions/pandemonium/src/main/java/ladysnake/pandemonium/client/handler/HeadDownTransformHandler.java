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
package ladysnake.pandemonium.client.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.requiem.api.v1.event.minecraft.client.ApplyCameraTransformsCallback;
import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;

public class HeadDownTransformHandler implements ApplyCameraTransformsCallback {
    @Override
    public void applyCameraTransformations(Camera camera, float tickDelta) {
        Entity focusedEntity = camera.getFocusedEntity();
        if (focusedEntity instanceof PlayerEntity && !camera.isThirdPerson()) {
            Entity possessed = ((RequiemPlayer) focusedEntity).getPossessionComponent().getPossessedEntity();
            if (possessed instanceof ShulkerEntity && ((ShulkerEntity) possessed).getAttachedFace() == Direction.UP || possessed instanceof BatEntity && ((BatEntity) possessed).isRoosting()) {
                GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }
        }
    }
}
