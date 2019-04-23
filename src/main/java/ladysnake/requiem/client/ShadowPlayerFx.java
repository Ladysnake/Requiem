package ladysnake.requiem.client;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.satin.api.event.EntitiesPreRenderCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public final class ShadowPlayerFx implements EntitiesPreRenderCallback, ShaderEffectRenderCallback {
    public static final Identifier SHADOW_PLAYER_SHADER_ID = Requiem.id("shaders/post/shadow_player.json");
    public static final Identifier DESATURATE_SHADER_ID = new Identifier("minecraft", "shaders/post/desaturate.json");

    public static final ShadowPlayerFx INSTANCE = new ShadowPlayerFx();
    public static final int ETHEREAL_DESATURATE_RANGE = 16;
    public static final int ETHEREAL_DESATURATE_RANGE_SQ = ETHEREAL_DESATURATE_RANGE * ETHEREAL_DESATURATE_RANGE;

    private final MinecraftClient client = MinecraftClient.getInstance();
    public final ManagedShaderEffect shadowPlayerEffect = ShaderEffectManager.getInstance().manage(SHADOW_PLAYER_SHADER_ID, this::assignDepthTexture);
    private final ManagedShaderEffect desaturateEffect = ShaderEffectManager.getInstance().manage(DESATURATE_SHADER_ID);

    @Nullable
    private GlFramebuffer playersFramebuffer;
    private boolean renderedSoulPlayers;
    private boolean nearEthereal;

    void registerCallbacks() {
        EntitiesPreRenderCallback.EVENT.register(this);
        ShaderEffectRenderCallback.EVENT.register(this);
        ClientTickCallback.EVENT.register(this::update);
    }

    private void update(MinecraftClient client) {
        if (client.player != null) {
            PlayerEntity closestEtherealPlayer = client.world.getClosestPlayer(client.player.x, client.player.y, client.player.z, ETHEREAL_DESATURATE_RANGE, p -> p != client.player && ((RequiemPlayer)p).getRemnantState().isIncorporeal());
             this.nearEthereal = closestEtherealPlayer != null;
            if (nearEthereal) {
                float distanceSqToEthereal = (float) client.player.squaredDistanceTo(closestEtherealPlayer.x, closestEtherealPlayer.y, closestEtherealPlayer.z);
                this.desaturateEffect.setUniformValue("Saturation", 0.8f * (distanceSqToEthereal / ETHEREAL_DESATURATE_RANGE_SQ));
            }
        }
    }

    private void assignDepthTexture(ManagedShaderEffect shader) {
        client.getFramebuffer().beginWrite(false);
        int depthTexture = ((ReadableDepthFramebuffer)client.getFramebuffer()).getCurrentDepthTexture();
        if (depthTexture > -1) {
            this.playersFramebuffer = Objects.requireNonNull(shader.getShaderEffect()).getSecondaryTarget("players");
            this.playersFramebuffer.beginWrite(false);
            // Use the same depth texture for our framebuffer as the main one
            GLX.glFramebufferTexture2D(GLX.GL_FRAMEBUFFER, GLX.GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0);
        }
    }

    public void beginPlayersFbWrite() {
        if (this.playersFramebuffer != null) {
            this.renderedSoulPlayers = true;
            this.playersFramebuffer.beginWrite(false);
        }
    }

    @Override
    public void beforeEntitiesRender(Camera camera, VisibleRegion frustum, float tickDelta) {
        if (this.playersFramebuffer == null) {
            try {
                this.shadowPlayerEffect.initialize();
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to initialize shader effect", e);
            }
        }
        this.renderedSoulPlayers = false;
        this.playersFramebuffer.beginWrite(false);
        GlStateManager.clearColor(this.playersFramebuffer.clearColor[0], this.playersFramebuffer.clearColor[1], this.playersFramebuffer.clearColor[2], this.playersFramebuffer.clearColor[3]);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

        this.client.getFramebuffer().beginWrite(false);
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

}
