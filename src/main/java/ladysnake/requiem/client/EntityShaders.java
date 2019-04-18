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
package ladysnake.requiem.client;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.WaterCreatureEntity;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class EntityShaders {

    public static void pickShader(Entity camera, Consumer<Identifier> loadShaderFunc, @SuppressWarnings("unused") Supplier<ShaderEffect> appliedShaderGetter) {
        if (camera instanceof RequiemPlayer) {
            Entity possessed = (Entity) ((RequiemPlayer)camera).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                MinecraftClient.getInstance().gameRenderer.onCameraEntitySet(possessed);
            }
        } else if (camera instanceof WaterCreatureEntity) {
            loadShaderFunc.accept(RequiemFx.FISH_EYE_SHADER_ID);
        }
    }

}
