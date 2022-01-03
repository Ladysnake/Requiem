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

import ladysnake.requiem.api.v1.event.IdentifyingEvent;
import net.minecraft.client.util.math.MatrixStack;

@FunctionalInterface
public interface CrosshairRenderCallback {

    void onCrosshairRender(MatrixStack matrices, int scaledWidth, int scaledHeight);

    IdentifyingEvent<CrosshairRenderCallback> EVENT = new IdentifyingEvent<>(CrosshairRenderCallback.class,
            (listeners) -> (MatrixStack matrices, int scaledWidth, int scaledHeight) -> {
                for (CrosshairRenderCallback handler : listeners) {
                    handler.onCrosshairRender(matrices, scaledWidth, scaledHeight);
                }
            });

}
