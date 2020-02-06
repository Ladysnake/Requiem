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
