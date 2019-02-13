package ladysnake.dissolution.mixin.client.render;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow public abstract ShaderEffect getShader();

    @SuppressWarnings("UnresolvedMixinReference") // Synthetic method
    @Inject(
            // Inject into the synthetic method corresponding to the lambda in updateTargetedEntity
            method = "method_18144",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true
    )
    private static void unselectPossessedEntity(Entity tested, CallbackInfoReturnable<Boolean> info) {
        Entity camera = MinecraftClient.getInstance().getCameraEntity();
        if (camera instanceof DissolutionPlayer && ((DissolutionPlayer) camera).getPossessionComponent().isPossessing()) {
            info.setReturnValue(false);
        }
    }
}
