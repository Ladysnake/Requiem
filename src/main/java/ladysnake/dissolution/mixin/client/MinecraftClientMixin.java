package ladysnake.dissolution.mixin.client;

import ladysnake.satin.client.event.ClientLoadingEvent;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.client.MinecraftClient;
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

    @Inject(
            at = @At("RETURN"),
            method = "init"
    )
    public void hookResourceManager(CallbackInfo info) {
        Consumer<ReloadableResourceManager>[] handlers = ((HandlerArray<Consumer<ReloadableResourceManager>>) ClientLoadingEvent.RESOURCE_MANAGER).getBackingArray();
        for (Consumer<ReloadableResourceManager> handler : handlers) {
            handler.accept(this.resourceManager);
        }
    }


}
