package ladysnake.satin.mixin.client;

import ladysnake.satin.client.event.ResolutionChangeCallback;
import ladysnake.satin.client.event.ResourceManagerLoadedCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow private ReloadableResourceManager resourceManager;

    @Shadow public Window window;

    @Inject(
            at = @At("RETURN"),
            method = "init"
    )
    private void hookResourceManager(CallbackInfo info) {
        ResourceManagerLoadedCallback.EVENT.invoker().onResourceManagerLoaded(this.resourceManager);
    }

    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    private void hookResolutionChanged(CallbackInfo info) {
        int width = this.window.getFramebufferWidth();
        int height = this.window.getFramebufferHeight();
        ResolutionChangeCallback.EVENT.invoker().onWindowResized(width, height);
    }
}
