package ladysnake.dissolution.mixin.client;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import ladysnake.satin.client.event.RenderEvent;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Final @Shadow private MinecraftClient client;

    /**
     * Fires {@link RenderEvent#SHADER_EFFECT}
     */
    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;drawFramebuffer()V",
                    shift = AFTER
            ),
            method = "method_3192"
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
}
