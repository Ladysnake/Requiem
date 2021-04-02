package ladysnake.pandemonium.mixin.common.entity.mob;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.boss.WitherEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherEntity.class)
public class WitherEntityMixin {
    @Inject(method = "method_6877", at = @At("HEAD"), cancellable = true)
    private void cancelAttack(int headIndex, double d, double e, double f, boolean bl, CallbackInfo ci) {
        if (((Possessable) this).isBeingPossessed()) {
            ci.cancel();
        }
    }
}
