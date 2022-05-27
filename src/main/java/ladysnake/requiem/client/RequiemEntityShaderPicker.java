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

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RequiemEntityShaderPicker implements PickEntityShaderCallback {
    private static final boolean haemaAvailable = FabricLoader.getInstance().isModLoaded("haema");

    public static final Identifier DICHROMATIC_SHADER_ID = shader("dichromatic");
    public static final Identifier TETRACHROMATIC_SHADER_ID = shader("tetrachromatic");
    public static final Identifier VAMPIRE_SHADER_ID = new Identifier("haema", "shaders/post/vampirevision.json");

    public static final Identifier BEE_SHADER_ID = shader("bee");
    public static final Identifier DOLPHIN_SHADER_ID = shader("dolphin");
    public static final Identifier FISH_EYE_SHADER_ID = shader("fish_eye");

    public static final Identifier MOOSHROOM_SHADER_ID = shader("mooshroom");

    public void registerCallbacks() {
        PickEntityShaderCallback.EVENT.register(this);
    }

    @Override
    public void pickEntityShader(@Nullable Entity camera, Consumer<Identifier> loadShaderFunc, Supplier<ShaderEffect> appliedShaderGetter) {
        if (camera == null) return;
        // make players use their possessed entity's shader
        Entity possessed = PossessionComponent.getHost(camera);
        if (possessed != null) {
            MinecraftClient.getInstance().gameRenderer.onCameraEntitySet(possessed);
        } else if (appliedShaderGetter.get() == null) {
            if (camera.getType().isIn(RequiemEntityTypeTags.DICHROMATS)) {
                loadShaderFunc.accept(DICHROMATIC_SHADER_ID);
            } else if (camera.getType().isIn(RequiemEntityTypeTags.TETRACHROMATS)) {
                loadShaderFunc.accept(TETRACHROMATIC_SHADER_ID);
            } else if (haemaAvailable && camera.getType().isIn(RequiemEntityTypeTags.HEMERALOPES)) {
                loadShaderFunc.accept(VAMPIRE_SHADER_ID);
            } else if (camera instanceof BeeEntity) {
                loadShaderFunc.accept(BEE_SHADER_ID);
            } else if (camera instanceof MooshroomEntity) {
                loadShaderFunc.accept(MOOSHROOM_SHADER_ID);
            } else if (camera instanceof DolphinEntity) {
                loadShaderFunc.accept(DOLPHIN_SHADER_ID);
            } else if (camera instanceof WaterCreatureEntity) {
                loadShaderFunc.accept(FISH_EYE_SHADER_ID);
            }
        }
    }

    private static Identifier shader(String id) {
        return Requiem.id("shaders/post/" + id + ".json");
    }
}
