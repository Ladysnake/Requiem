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

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.satin.api.event.PostWorldRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.util.GlMatrices;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ZaWorldFx implements PostWorldRenderCallback {

    public static final Identifier ZA_WARUDO_SHADER_ID = Requiem.id("shaders/post/za_warudo.json");
    public static final ZaWorldFx INSTANCE = new ZaWorldFx();

    private int ticks;
    private float prevRadius;
    private float radius;
    private boolean renderingEffect;
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final ManagedShaderEffect shader = ShaderEffectManager.getInstance().manage(ZA_WARUDO_SHADER_ID, shader -> {
        MinecraftClient mc = MinecraftClient.getInstance();
        shader.setSamplerUniform("DepthSampler", ((ReadableDepthFramebuffer)mc.getFramebuffer()).getStillDepthMap());
        shader.setUniformValue("ViewPort", 0, 0, mc.window.getFramebufferWidth(), mc.window.getFramebufferHeight());
    });

    void registerCallbacks() {
        PostWorldRenderCallback.EVENT.register(this);
        ClientTickCallback.EVENT.register(this::update);
    }

    private void update(MinecraftClient client) {
        if (client.player != null && ((RequiemPlayer) client.player).getDeathSuspender().isLifeTransient()) {
            if (!this.renderingEffect) {
                this.shader.setUniformValue("OuterSat", 1f);
                this.ticks = 0;
                this.prevRadius = this.radius = 0;
                this.renderingEffect = true;
                client.player.world.playSound(client.player.x, client.player.y, client.player.z, RequiemSoundEvents.EFFECT_TIME_STOP, SoundCategory.PLAYERS, 1.0F, 1.0F, false);
            }
            this.ticks++;
            this.prevRadius = this.radius;
            final float expansionRate = 4f;
            final int inversion = 100 / (int) expansionRate;
            if (ticks == inversion) {
                this.shader.setUniformValue("OuterSat", 0.3f);
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
        return this.ticks > 60;
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (renderingEffect) {
            shader.setUniformValue("STime", (ticks + tickDelta) / 20f);
            shader.setUniformValue("InverseTransformMatrix", GlMatrices.getInverseTransformMatrix(projectionMatrix));
            Vec3d cameraPos = camera.getPos();
            shader.setUniformValue("CameraPosition", (float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
            Entity e = camera.getFocusedEntity();
            shader.setUniformValue("Center", lerpf(e.x, e.prevX, tickDelta), lerpf(e.y, e.prevY, tickDelta), lerpf(e.z, e.prevZ, tickDelta));
            shader.setUniformValue("Radius", lerpf(radius, prevRadius, tickDelta));
            shader.render(tickDelta);
        }
    }

    private static float lerpf(double n, double prevN, float tickDelta) {
        return (float) MathHelper.lerp(tickDelta, prevN, n);
    }
}
