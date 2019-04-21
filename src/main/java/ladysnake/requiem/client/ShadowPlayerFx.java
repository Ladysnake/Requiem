package ladysnake.requiem.client;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.requiem.Requiem;
import ladysnake.satin.api.event.EntitiesPreRenderCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public final class ShadowPlayerFx implements EntitiesPreRenderCallback, ShaderEffectRenderCallback {
    public static final Identifier SHADOW_PLAYER_ID = Requiem.id("shaders/post/shadow_player.json");

    public static final ShadowPlayerFx INSTANCE = new ShadowPlayerFx();

    private final MinecraftClient client = MinecraftClient.getInstance();
    public final ManagedShaderEffect shadowPlayerEffect = ShaderEffectManager.getInstance().manage(SHADOW_PLAYER_ID, this::assignDepthTexture);

    @Nullable
    private GlFramebuffer playersFramebuffer;
    private boolean renderedSoulPlayers;

    void registerCallbacks() {
        ReadableDepthFramebuffer.useFeature();
        EntitiesPreRenderCallback.EVENT.register(this);
        ShaderEffectRenderCallback.EVENT.register(this);
    }

    private void assignDepthTexture(ManagedShaderEffect shader) {
        client.getFramebuffer().beginWrite(false);
        int depthTexture = ((ReadableDepthFramebuffer)client.getFramebuffer()).getCurrentDepthTexture();
        this.playersFramebuffer = Objects.requireNonNull(shader.getShaderEffect()).getSecondaryTarget("players");
        this.playersFramebuffer.beginWrite(false);
        // Use the same depth texture for our framebuffer as the main one
        GLX.glFramebufferTexture2D(GLX.GL_FRAMEBUFFER, GLX.GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0);
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
    }

}
