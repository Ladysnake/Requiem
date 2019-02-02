package ladysnake.dissolution.client;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.satin.client.event.RenderEvent;
import ladysnake.satin.client.shader.ManagedShaderEffect;
import ladysnake.satin.client.shader.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createPossessionRequestPacket;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendToServer;

public final class DissolutionEffects implements RenderEvent.PreBlockEntities, RenderEvent.ResolutionChangeListener {
    public static final Identifier SPECTRE_SHADER_ID = Dissolution.id("shaders/post/spectre.json");
    public static final Identifier FISH_EYE_SHADER_ID = Dissolution.id("shaders/post/fish_eye.json");

    public static final DissolutionEffects INSTANCE = new DissolutionEffects();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final ManagedShaderEffect spectreShader = ShaderEffectManager.manage(SPECTRE_SHADER_ID);
    private final ManagedShaderEffect fishEyeShader = ShaderEffectManager.manage(FISH_EYE_SHADER_ID);
    private GlFramebuffer framebuffer;

    public int fishEyeAnimation = -1;
    @Nullable
    public WeakReference<Entity> possessed;

    public void tick(@SuppressWarnings("unused") MinecraftClient client) {
        Entity possessed = this.possessed != null ? this.possessed.get() : null;
        if (possessed != null) {
            if (--fishEyeAnimation == 3) {
                sendToServer(createPossessionRequestPacket(possessed));
            } else if (fishEyeAnimation == 0) {
                this.possessed = null;
            }
        }
    }

    public void beginFishEyeAnimation(Entity possessed) {
        this.fishEyeAnimation = 10;
        this.possessed = new WeakReference<>(possessed);
        possessed.world.playSound(mc.player, possessed.x, possessed.y, possessed.z, SoundEvents.ENTITY_VEX_AMBIENT, SoundCategory.PLAYER, 2, 0.6f);
    }

    public void renderShaders(float tickDelta) {
        if (fishEyeAnimation > 0) {
            fishEyeShader.setUniformValue("Slider", (fishEyeAnimation - tickDelta) / 40 + 0.25f);
            fishEyeShader.render(tickDelta);
            if (this.possessed != null && this.framebuffer != null) {
                GlStateManager.enableBlend();
                GlStateManager.blendFuncSeparate(GlStateManager.class_1033.SRC_ALPHA, GlStateManager.class_1027.ONE_MINUS_SRC_ALPHA, GlStateManager.class_1033.ZERO, GlStateManager.class_1027.ONE);
                this.framebuffer.draw(this.mc.window.getWidth(), this.mc.window.getHeight(), false);
                MinecraftClient.getInstance().worldRenderer.drawEntityOutlinesFramebuffer();
            }
        }
        if (RemnantState.getIfRemnant(mc.player).filter(RemnantState::isIncorporeal).isPresent()) {
            spectreShader.render(tickDelta);
        }
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    public void onPreRenderBlockEntities(Entity camera, VisibleRegion frustum, float tickDelta) {
        Entity possessed = this.possessed != null ? this.possessed.get() : null;
        if (possessed != null) {
            if (this.framebuffer == null) {
                this.framebuffer = new GlFramebuffer(mc.window.getWidth(), mc.window.getHeight(), true, MinecraftClient.isSystemMac);
            }
            this.framebuffer.clear(MinecraftClient.isSystemMac);
            GlStateManager.disableFog();
            this.framebuffer.beginWrite(false);
            this.mc.getEntityRenderManager().render(possessed, tickDelta, true);
            GlStateManager.enableLighting();
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableDepthTest();
            GlStateManager.enableAlphaTest();
            this.mc.getFramebuffer().beginWrite(false);
        }
    }

    @Override
    public void onWindowResized(int newWidth, int newHeight) {
        if (this.framebuffer != null) {
            this.framebuffer.resize(newWidth, newHeight, MinecraftClient.isSystemMac);
        }
    }
}
