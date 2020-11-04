package ladysnake.requiem.mixin.common.possession.possessed;

import ladysnake.requiem.common.entity.ai.DisableableBrain;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Brain.class)
public abstract class BrainMixin implements DisableableBrain {
    @Unique
    private boolean disabled;

    @Override
    public void requiem_setDisabled(boolean disabled) {
        this.disabled = true;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(ServerWorld world, LivingEntity entity, CallbackInfo ci) {
        if (this.disabled) {
            ci.cancel();
        }
    }

    @Inject(method = "isMemoryInState", at = @At("HEAD"), cancellable = true)
    private void isMemoryInState(MemoryModuleType<?> type, MemoryModuleState state, CallbackInfoReturnable<Boolean> cir) {
        if (this.disabled) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getOptionalMemory", at = @At("HEAD"), cancellable = true)
    private void getOptionalMemory(MemoryModuleType<?> type, CallbackInfoReturnable<Optional<?>> cir) {
        if (this.disabled) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
