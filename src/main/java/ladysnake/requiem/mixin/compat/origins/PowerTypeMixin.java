package ladysnake.requiem.mixin.compat.origins;

import io.github.apace100.origins.power.PowerType;
import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnusedMixin")    // compat mixin
@Mixin(PowerType.class)
public abstract class PowerTypeMixin {
    @Inject(method = "isActive", at = @At("RETURN"), cancellable = true)
    private void cancelSoulPowers(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && entity instanceof PlayerEntity && RequiemPlayer.from((PlayerEntity) entity).asRemnant().isSoul()) {
            cir.setReturnValue(false);
        }
    }
}
