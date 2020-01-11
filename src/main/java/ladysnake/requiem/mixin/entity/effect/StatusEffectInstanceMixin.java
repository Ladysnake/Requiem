package ladysnake.requiem.mixin.entity.effect;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectInstance.class)
public abstract class StatusEffectInstanceMixin {
    @Shadow
    public abstract StatusEffect getEffectType();

    @Shadow
    private int duration;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffectInstance;updateDuration()I"))
    private void preventSoulboundCountdown(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof RequiemPlayer && ((RequiemPlayer) livingEntity).asRemnant().isSoul()) {
            if (SoulbindingRegistry.instance().isSoulbound(this.getEffectType())) {
                this.duration++; // revert the duration decrement
            }
        }
    }
}
