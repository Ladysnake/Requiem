package ladysnake.pandemonium.mixin.common.entity.mob;

import ladysnake.pandemonium.common.entity.ability.ExtendedWololoGoal;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.EvokerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EvokerEntity.WololoGoal.class)
public abstract class WololoGoalMixin implements ExtendedWololoGoal {
    @Shadow
    @Final
    private TargetPredicate convertibleSheepPredicate;

    @Override
    public @NotNull TargetPredicate requiem_getConvertibleSheepPredicate() {
        return this.convertibleSheepPredicate;
    }

    @Inject(method = "canStart", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/mob/EvokerEntity$WololoGoal;convertibleSheepPredicate:Lnet/minecraft/entity/ai/TargetPredicate;"), cancellable = true)
    private void canStart(CallbackInfoReturnable<Boolean> cir) {
        if (this.requiem_hasValidTarget()) {
            cir.setReturnValue(true);
        }
    }
}
