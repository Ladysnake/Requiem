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
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.satin.api.event.PostWorldRenderCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform3f;
import ladysnake.satin.api.managed.uniform.UniformMat4;
import ladysnake.satin.api.util.GlMatrices;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class ZaWorldFx implements PostWorldRenderCallback, ClientTickEvents.EndTick, ShaderEffectRenderCallback {

    public static final Identifier ZA_WARUDO_SHADER_ID = Requiem.id("shaders/post/za_warudo.json");

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final Matrix4f projectionMatrix = new Matrix4f();

    private final ManagedShaderEffect shader = ShaderEffectManager.getInstance().manage(ZA_WARUDO_SHADER_ID, shader -> {
        shader.setSamplerUniform("DepthSampler", ((ReadableDepthFramebuffer) this.mc.getFramebuffer()).getStillDepthMap());
        shader.setUniformValue("ViewPort", 0, 0, this.mc.getWindow().getFramebufferWidth(), this.mc.getWindow().getFramebufferHeight());
    });

    private final Uniform1f uniformOuterSat = shader.findUniform1f("OuterSat");
    private final Uniform1f uniformSTime = shader.findUniform1f("STime");
    private final UniformMat4 uniformInverseTransformMatrix = shader.findUniformMat4("InverseTransformMatrix");
    private final Uniform3f uniformCameraPosition = shader.findUniform3f("CameraPosition");
    private final Uniform3f uniformCenter = shader.findUniform3f("Center");
    private final Uniform1f uniformRadius = shader.findUniform1f("Radius");

    private int ticks;
    private float prevRadius;
    private float radius;
    private boolean renderingEffect;

    void registerCallbacks() {
        PostWorldRenderCallback.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        ShaderEffectRenderCallback.EVENT.register(this);
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (client.player != null && DeathSuspender.get(client.player).isLifeTransient()) {
            if (!this.renderingEffect) {
                this.uniformOuterSat.set(1f);
                this.ticks = 0;
                this.prevRadius = this.radius = 0;
                this.renderingEffect = true;
                client.player.world.playSound(client.player.getX(), client.player.getY(), client.player.getZ(), RequiemSoundEvents.EFFECT_TIME_STOP, SoundCategory.PLAYERS, 1.0F, 1.0F, false);
            }
            this.ticks++;
            this.prevRadius = this.radius;
            final float expansionRate = 4f;
            final int inversion = 100 / (int) expansionRate;
            if (ticks == inversion) {
                this.uniformOuterSat.set(0.3f);
            } else if (ticks < inversion) {
                this.radius += expansionRate;
            } else if (ticks < 2 * inversion) {
                this.radius -= expansionRate;
            }
        } else {
            this.renderingEffect = false;
        }
    }

    public boolean hasFinishedAnimation() {
        return !this.renderingEffect || this.ticks > 60;
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (renderingEffect) {
            uniformSTime.set((ticks + tickDelta) / 20f);
            uniformInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(projectionMatrix));
            Vec3d cameraPos = camera.getPos();
            uniformCameraPosition.set((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
            Entity e = camera.getFocusedEntity();
            uniformCenter.set(lerpf(e.getX(), e.prevX, tickDelta), lerpf(e.getY(), e.prevY, tickDelta), lerpf(e.getZ(), e.prevZ, tickDelta));
            uniformRadius.set(lerpf(radius, prevRadius, tickDelta));
        }
    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        if (this.renderingEffect) {
            shader.render(tickDelta);
        }
    }

    private static float lerpf(double n, double prevN, float tickDelta) {
        return (float) MathHelper.lerp(tickDelta, prevN, n);
    }
}
