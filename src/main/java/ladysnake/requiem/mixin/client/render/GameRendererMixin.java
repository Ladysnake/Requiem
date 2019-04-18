package ladysnake.requiem.mixin.client.render;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

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
        if (camera instanceof RequiemPlayer && ((RequiemPlayer) camera).getPossessionComponent().getPossessedEntity() == tested) {
            info.setReturnValue(false);
        }
    }
}
