package ladysnake.requiem.mixin.client.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ClientEntityMixin {
    @Inject(method = "method_20448", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInsideWater()Z"), cancellable = true)
    private void isCrawling(CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof RequiemPlayer && ((RequiemPlayer) this).getRemnantState().isIncorporeal()) {
            cir.setReturnValue(true);
        }
    }
}
