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

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.satin.api.event.EntitiesPreRenderCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.util.RenderLayerHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public final class ShadowPlayerFx implements EntitiesPreRenderCallback, ShaderEffectRenderCallback {
    public static final Identifier SHADOW_PLAYER_SHADER_ID = Requiem.id("shaders/post/shadow_player.json");
    public static final Identifier DESATURATE_SHADER_ID = new Identifier("minecraft", "shaders/post/desaturate.json");

    public static final int ETHEREAL_DESATURATE_RANGE = 12;
    public static final int ETHEREAL_DESATURATE_RANGE_SQ = ETHEREAL_DESATURATE_RANGE * ETHEREAL_DESATURATE_RANGE;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final ManagedShaderEffect shadowPlayerEffect = ShaderEffectManager.getInstance().manage(SHADOW_PLAYER_SHADER_ID, this::assignDepthTexture);
    private final ManagedShaderEffect desaturateEffect = ShaderEffectManager.getInstance().manage(DESATURATE_SHADER_ID);

    private final ManagedFramebuffer playersFramebuffer = shadowPlayerEffect.getTarget("players");
    private final RenderPhase.Target target = new RenderPhase.Target(
        "requiem:shadow_players_target",
        this::beginPlayersFbWrite,
        () -> {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
            RenderSystem.depthMask(true);
        }
    );
    private boolean renderedSoulPlayers;
    private boolean nearEthereal;
    private final Uniform1f uniformSaturation = this.desaturateEffect.findUniform1f("Saturation");

    void registerCallbacks() {
        EntitiesPreRenderCallback.EVENT.register(this);
        ShaderEffectRenderCallback.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this::update);
    }

    private void update(MinecraftClient client) {
        if (client.player != null) {
            PlayerEntity closestEtherealPlayer = client.player.world.getClosestPlayer(
                client.player.getX(),
                client.player.getY(),
                client.player.getZ(),
                ETHEREAL_DESATURATE_RANGE,
                p -> p != client.player && RemnantComponent.isIncorporeal(p)
            );
            this.nearEthereal = closestEtherealPlayer != null;
            if (nearEthereal) {
                float distanceSqToEthereal = (float) client.player.squaredDistanceTo(closestEtherealPlayer.getX(), closestEtherealPlayer.getY(), closestEtherealPlayer.getZ());
                uniformSaturation.set(0.8f * (distanceSqToEthereal / ETHEREAL_DESATURATE_RANGE_SQ));
            }
        }
    }

    private void assignDepthTexture(ManagedShaderEffect shader) {
        client.getFramebuffer().beginWrite(false);
        int depthTexture = client.getFramebuffer().getDepthAttachment();
        if (depthTexture > -1) {
            playersFramebuffer.beginWrite(false);
            // Use the same depth texture for our framebuffer as the main one
            GlStateManager.framebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0);
        }
    }

    public void beginPlayersFbWrite() {
        Framebuffer playersFramebuffer = this.playersFramebuffer.getFramebuffer();
        if (playersFramebuffer != null) {
            playersFramebuffer.beginWrite(false);
            RenderSystem.depthMask(false);
            if (!this.renderedSoulPlayers) {
                // no depth clearing
                RenderSystem.clearColor(playersFramebuffer.clearColor[0], playersFramebuffer.clearColor[1], playersFramebuffer.clearColor[2], playersFramebuffer.clearColor[3]);
                RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

                this.renderedSoulPlayers = true;
            }
        }
    }

    @Override
    public void beforeEntitiesRender(Camera camera, Frustum frustum, float tickDelta) {
        if (!this.shadowPlayerEffect.isInitialized()) {
            try {
                this.shadowPlayerEffect.initialize();
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to initialize shader effect", e);
            }
        }
        this.renderedSoulPlayers = false;
    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        if (this.renderedSoulPlayers) {
            shadowPlayerEffect.render(tickDelta);
        }
        if (this.nearEthereal) {
            this.desaturateEffect.render(tickDelta);
        }
    }

    public RenderLayer getRenderLayer(RenderLayer base) {
        return RenderLayerHelper.copy(base, "requiem:shadow_players", builder -> builder.target(target));
    }
}
