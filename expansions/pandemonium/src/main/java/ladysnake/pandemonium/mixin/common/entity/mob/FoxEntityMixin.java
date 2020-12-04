package ladysnake.pandemonium.mixin.common.entity.mob;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoxEntity.class)
public abstract class FoxEntityMixin implements Possessable {
    @Shadow
    protected abstract void stopActions();

    @Shadow
    public abstract void setCrouching(boolean crouching);

    @Override
    public void onPossessorSet(@Nullable PlayerEntity possessor) {
        this.stopActions();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/FoxEntity;isInSneakingPose()Z"))
    private void tick(CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            this.setCrouching(possessor.isSneaking());
        }
    }
}
