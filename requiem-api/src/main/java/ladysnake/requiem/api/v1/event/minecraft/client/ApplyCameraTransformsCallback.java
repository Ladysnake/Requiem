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
package ladysnake.requiem.api.v1.event.minecraft.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

public interface ApplyCameraTransformsCallback {

    void applyCameraTransformations(Camera camera, MatrixStack matrices, float tickDelta);

    Event<ApplyCameraTransformsCallback> EVENT = EventFactory.createArrayBacked(ApplyCameraTransformsCallback.class,
            (listeners) -> (camera, matrices, tickDelta) -> {
                for (ApplyCameraTransformsCallback handler : listeners) {
                    handler.applyCameraTransformations(camera, matrices, tickDelta);
                }
            });

}
