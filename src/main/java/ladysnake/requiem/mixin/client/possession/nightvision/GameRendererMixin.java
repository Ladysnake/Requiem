package ladysnake.requiem.mixin.client.possession.nightvision;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)    // Have to cancel at head, otherwise NPE if no night vision
    private static void getNightVisionStrength(LivingEntity livingEntity, float f, CallbackInfoReturnable<Float> cir) {
        if (RemnantComponent.isIncorporeal(livingEntity)) {
            cir.setReturnValue(1F);
        }
    }
}
