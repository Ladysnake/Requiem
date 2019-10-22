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

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.minecraft.client.ApplyCameraTransformsCallback;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MatrixStack;

public class HeadDownTransformHandler implements ApplyCameraTransformsCallback {
    @Override
    public void applyCameraTransformations(Camera camera, MatrixStack matrices, float tickDelta) {
        Entity focusedEntity = camera.getFocusedEntity();
        if (focusedEntity instanceof PlayerEntity && !camera.isThirdPerson()) {
            Entity possessed = ((RequiemPlayer) focusedEntity).asPossessor().getPossessedEntity();
            if (possessed instanceof ShulkerEntity && ((ShulkerEntity) possessed).getAttachedFace() == Direction.UP || possessed instanceof BatEntity && ((BatEntity) possessed).isRoosting()) {
                matrices.multiply(Vector3f.POSITIVE_X.getRotationQuaternion(180.0F));
                matrices.multiply(Vector3f.POSITIVE_Y.getRotationQuaternion(180.0F));
            }
        }
    }
}
