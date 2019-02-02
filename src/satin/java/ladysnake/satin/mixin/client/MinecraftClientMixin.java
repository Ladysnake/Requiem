package ladysnake.satin.mixin.client;

import ladysnake.satin.client.event.ClientLoadingEvent;
import ladysnake.satin.client.event.RenderEvent;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow private ReloadableResourceManager resourceManager;

    @Shadow public Window window;

    @Inject(
            at = @At("RETURN"),
            method = "init"
    )
    private void hookResourceManager(CallbackInfo info) {
        Consumer<ReloadableResourceManager>[] handlers = ((HandlerArray<Consumer<ReloadableResourceManager>>) ClientLoadingEvent.RESOURCE_MANAGER).getBackingArray();
        for (Consumer<ReloadableResourceManager> handler : handlers) {
            handler.accept(this.resourceManager);
        }
    }

    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    private void hookResolutionChanged(CallbackInfo info) {
        int width = this.window.getFramebufferWidth();
        int height = this.window.getFramebufferHeight();
        for (RenderEvent.ResolutionChangeListener handler : ((HandlerArray<RenderEvent.ResolutionChangeListener>)RenderEvent.RESOLUTION_CHANGED).getBackingArray()) {
            handler.onWindowResized(width, height);
        }
    }
}
