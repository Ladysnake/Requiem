package ladysnake.satin.mixin.client;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import ladysnake.satin.client.event.RenderEvent;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Function;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow private ShaderEffect shader;

    @Shadow protected abstract void loadShader(Identifier identifier_1);

    @Final @Shadow private MinecraftClient client;

    /**
     * Fires {@link RenderEvent#SHADER_EFFECT}
     */
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V",
                    shift = AFTER
            ),
            method = "render"
    )
    public void hookShaderRender(float tickDelta, long nanoTime, boolean renderLevel, CallbackInfo info) {
        FloatConsumer[] handlers = ((HandlerArray<FloatConsumer>)RenderEvent.SHADER_EFFECT).getBackingArray();
        if (handlers.length > 0) {
            Profiler profiler = this.client.getProfiler();
            profiler.push("shaders");
            for (FloatConsumer handler : handlers) {
                handler.accept(tickDelta);
            }
            profiler.pop();
        }
    }

    @Inject(method = "onCameraEntitySet", at = @At(value = "RETURN", ordinal = 1))
    public void useCustomEntityShader(@Nullable Entity entity, CallbackInfo info) {
        if (this.shader == null) {
            for (Function<Entity, Identifier> handler : ((HandlerArray<Function<Entity, Identifier>>) RenderEvent.PICK_ENTITY_SHADER).getBackingArray()) {
                Identifier id = handler.apply(entity);
                if (id != null) {
                    this.loadShader(id);
                }
                // Allow listeners to set the shader themselves if they need to configure it
                if (this.shader != null) {
                    return;
                }
            }
        }
    }
}
