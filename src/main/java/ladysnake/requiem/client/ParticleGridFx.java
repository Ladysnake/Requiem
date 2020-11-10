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
package ladysnake.requiem.client;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.satin.api.event.PostWorldRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class ParticleGridFx implements PostWorldRenderCallback, ClientTickEvents.EndTick {

    public static final Identifier PHASING_SHADER_ID = Requiem.id("shaders/post/phasing.json");

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final Matrix4f projectionMatrix = new Matrix4f();

    private final ManagedShaderEffect shader = ShaderEffectManager.getInstance().manage(PHASING_SHADER_ID, shader -> {
        shader.setUniformValue("ViewPort", 0, 0, this.mc.getWindow().getFramebufferWidth(), this.mc.getWindow().getFramebufferHeight());
    });

    private final Uniform1f uniformSTime = shader.findUniform1f("STime");

    private int ticks;
    private boolean renderingEffect;

    void registerCallbacks() {
        PostWorldRenderCallback.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (client.player != null && MovementAlterer.get(client.player).isNoClipping()) {
            if (!this.renderingEffect) {
                this.ticks = 0;
                this.renderingEffect = true;
                client.player.world.playSound(client.player.getX(), client.player.getY(), client.player.getZ(), RequiemSoundEvents.EFFECT_TIME_STOP, SoundCategory.PLAYERS, 1.0F, 1.0F, false);
            }
            this.ticks++;
        } else {
            this.renderingEffect = false;
        }
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (renderingEffect) {
            uniformSTime.set((ticks + tickDelta) / 20f);
            shader.render(tickDelta);
        }
    }
}
