package ladysnake.pandemonium.mixin.client.render;

import ladysnake.pandemonium.client.PandemoniumClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderCenter", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntities(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/VisibleRegion;F)V", shift = AFTER))
    private void renderSpecials(float tickDelta, long time, CallbackInfo ci) {
        PandemoniumClient.INSTANCE.soulWebRenderer.render(tickDelta, time);
    }
}
