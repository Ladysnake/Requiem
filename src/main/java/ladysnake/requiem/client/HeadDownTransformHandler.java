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
package ladysnake.requiem.client;

import ladysnake.requiem.api.v1.event.minecraft.client.ApplyCameraTransformsCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import javax.annotation.Nullable;

public class HeadDownTransformHandler implements ApplyCameraTransformsCallback {

    public static final Quaternion QUATERNION_180_X = Vec3f.POSITIVE_X.getDegreesQuaternion(180.0F);
    public static final Quaternion QUATERNION_180_Y = Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F);

    @Override
    public void applyCameraTransformations(Camera camera, MatrixStack matrices, float tickDelta) {
        if (!camera.isThirdPerson()) {
            Entity focusedEntity = camera.getFocusedEntity();
            if (focusedEntity != null && focusedEntity.getComponentContainer() != null) {
                Entity possessed = PossessionComponent.getHost(focusedEntity);
                if (isUpsideDown(possessed)) {
                    matrices.multiply(QUATERNION_180_X);
                    matrices.multiply(QUATERNION_180_Y);
                }
            }
        }
    }

    private static boolean isUpsideDown(@Nullable Entity possessed) {
        return possessed instanceof ShulkerEntity && ((ShulkerEntity) possessed).getAttachedFace() == Direction.UP || possessed instanceof BatEntity && ((BatEntity) possessed).isRoosting();
    }
}
