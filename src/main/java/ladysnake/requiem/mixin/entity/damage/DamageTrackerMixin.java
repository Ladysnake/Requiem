package ladysnake.requiem.mixin.entity.damage;

import ladysnake.requiem.api.v1.player.RequiemPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin {
    @Shadow @Final private LivingEntity entity;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void stopUpdating(CallbackInfo ci) {
        if (this.entity instanceof RequiemPlayer && ((RequiemPlayer) this.entity).getDeathSuspender().isLifeTransient()) {
            ci.cancel();
        }
    }
}
