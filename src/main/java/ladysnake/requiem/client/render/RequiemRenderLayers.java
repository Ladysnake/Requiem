/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.client.render;

import ladysnake.requiem.client.RequiemFx;
import ladysnake.requiem.client.ShadowPlayerFx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

public final class RequiemRenderLayers extends RenderPhase {
    private static final Target SHADOW_PLAYER_TARGET = new Target("requiem:shadow_player_target", () -> {
        Framebuffer playersFramebuffer = ShadowPlayerFx.INSTANCE.getPlayersFramebuffer();
        if (playersFramebuffer != null) {
            playersFramebuffer.beginWrite(false);
        }
    }, () -> {
        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
    });
    private static final Target ZOOM_ENTITY_TARGET = new Target("requiem:zoom_entity_target", () -> {
        RequiemFx.INSTANCE.getFramebuffer().beginWrite(false);
    }, () -> {
        RequiemFx.INSTANCE.getFramebuffer().clear(MinecraftClient.IS_SYSTEM_MAC);
        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
    });

    public static RenderLayer getShadowFx(Identifier texture) {
        return RenderLayer.of(
            "requiem:shadow_player",
            VertexFormats.POSITION_COLOR_TEXTURE,
            GL11.GL_QUADS,
            256,
            RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(texture, false, false))
                .cull(DISABLE_CULLING)
                .depthTest(ALWAYS_DEPTH_TEST)
                .alpha(ONE_TENTH_ALPHA)
                .texturing(DEFAULT_TEXTURING)
                .fog(NO_FOG)
                .target(SHADOW_PLAYER_TARGET)
                .build(false)
        );
    }

    public static RenderLayer getZoomFx(Identifier texture) {
        return RenderLayer.of(
            "requiem:zoom_entity",
            VertexFormats.POSITION_COLOR_TEXTURE,
            GL11.GL_QUADS,
            256,
            RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(texture, false, false))
                .cull(DISABLE_CULLING)
                .depthTest(ALWAYS_DEPTH_TEST)
                .alpha(ONE_TENTH_ALPHA)
                .texturing(DEFAULT_TEXTURING)
                .fog(NO_FOG)
                .target(ZOOM_ENTITY_TARGET)
                .build(false)
        );
    }

    private RequiemRenderLayers(String name, Runnable beginAction, Runnable endAction) {
        super(name, beginAction, endAction);
    }

}
