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
package ladysnake.requiem.client.render;

import ladysnake.requiem.Requiem;
import ladysnake.satin.api.managed.ManagedFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.lwjgl.opengl.GL11;

public final class RequiemRenderPhases extends RenderLayer {
    public static final RenderPhase.Alpha ZERO_ALPHA = RenderPhase.ZERO_ALPHA;
    public static final ManagedShaderEffect GHOST_PARTICLE_SHADER = ShaderEffectManager.getInstance().manage(Requiem.id("shaders/post/ghost_particles.json"));
    public static final ManagedFramebuffer GHOST_PARTICLE_FRAMEBUFFER = GHOST_PARTICLE_SHADER.getTarget("ghost_particles");
    public static final RenderLayer GHOST_PARTICLE_LAYER = RenderLayer.of(
        "requiem:ghost_particle",
        VertexFormats.POSITION_TEXTURE_COLOR_LIGHT,
        GL11.GL_QUADS,
        256,
        false,
        true,
        MultiPhaseParameters.builder()
            .texture(new Texture(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE, false, false))
            .alpha(RenderPhase.ONE_TENTH_ALPHA)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
            .writeMaskState(RenderPhase.COLOR_MASK)
            .target(new Target("requiem:ghost_particles_target",
                () -> GHOST_PARTICLE_FRAMEBUFFER.beginWrite(false),
                () -> MinecraftClient.getInstance().getFramebuffer().beginWrite(false)))
            .build(false)
    );

    private RequiemRenderPhases(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }
}
