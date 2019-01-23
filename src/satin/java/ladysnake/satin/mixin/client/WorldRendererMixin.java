package ladysnake.satin.mixin.client;

import ladysnake.satin.client.event.RenderEvent;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Inject(method = "renderEntities", at = @At(value = "CONSTANT", args = "stringValue=blockentities"))
    public void firePreRenderBlockEntities(Entity camera, VisibleRegion frustum, float tickDelta, CallbackInfo info) {
        for (RenderEvent.PreBlockEntities handler : ((HandlerArray< RenderEvent.PreBlockEntities>)RenderEvent.BLOCK_ENTITIES_RENDER).getBackingArray()) {
            handler.onPreRenderBlockEntities(camera, frustum, tickDelta);
        }
    }

}
