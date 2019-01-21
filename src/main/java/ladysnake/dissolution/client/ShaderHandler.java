package ladysnake.dissolution.client;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.remnant.RemnantHandler;
import ladysnake.satin.client.event.RenderEvent;
import ladysnake.satin.client.shader.ManagedShaderEffect;
import ladysnake.satin.client.shader.ShaderEffectManager;
import net.minecraft.class_856;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createPossessionRequestPacket;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendToServer;

public final class ShaderHandler implements FloatConsumer, RenderEvent.PreBlockEntities {
    public static final Identifier SPECTRE_SHADER_ID = Dissolution.id("shaders/post/spectre.json");
    public static final Identifier FISH_EYE_SHADER_ID = Dissolution.id("shaders/post/fish_eye.json");

    public static final ShaderHandler INSTANCE = new ShaderHandler();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final ManagedShaderEffect spectreShader = ShaderEffectManager.manage(SPECTRE_SHADER_ID);
    private final ManagedShaderEffect fishEyeShader = ShaderEffectManager.manage(FISH_EYE_SHADER_ID);
    private GlFramebuffer framebuffer;

    public int fishEyeAnimation = -1;
    public @Nullable Entity possessed;

    public void tick(@SuppressWarnings("unused") MinecraftClient client) {
        if (this.possessed != null && --fishEyeAnimation == 2) {
            sendToServer(createPossessionRequestPacket(possessed));
        }
    }

    public void beginFishEyeAnimation(Entity possessed) {
        this.fishEyeAnimation = 10;
        this.possessed = possessed;
    }

    @Override
    public void accept(float tickDelta) {
        if (RemnantHandler.get(mc.player).filter(RemnantHandler::isIncorporeal).isPresent()) {
            spectreShader.render(tickDelta);
        }
        if (fishEyeAnimation > 0) {
            fishEyeShader.setUniformValue("Slider", (fishEyeAnimation - tickDelta) / 40 + 0.25f);
            fishEyeShader.render(tickDelta);
            if (this.possessed != null && this.framebuffer != null) {
                GlStateManager.enableBlend();
                GlStateManager.blendFuncSeparate(GlStateManager.SrcBlendFactor.SRC_ALPHA, GlStateManager.DstBlendFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcBlendFactor.ZERO, GlStateManager.DstBlendFactor.ONE);
                this.framebuffer.draw(this.mc.window.getWindowWidth(), this.mc.window.getWindowHeight(), false);
                MinecraftClient.getInstance().renderer.drawFramebuffer();
            }
        }
    }

    @Override
    public void onPreRenderBlockEntities(Entity camera, class_856 frustum, float tickDelta) {
        if (this.possessed != null) {
            if (this.framebuffer == null) {
                this.framebuffer = new GlFramebuffer(mc.window.getWindowWidth(), mc.window.getWindowHeight(), true, MinecraftClient.isSystemMac);
            }
            this.framebuffer.clear(MinecraftClient.isSystemMac);
            GlStateManager.disableFog();
            this.framebuffer.beginWrite(false);
            GuiLighting.disable();


            GuiLighting.enable();
            GlStateManager.enableLighting();
            this.mc.getEntityRenderManager().render(this.possessed, tickDelta, false);
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableDepthTest();
            GlStateManager.enableAlphaTest();
            this.mc.getFramebuffer().beginWrite(false);
        }
    }


}
