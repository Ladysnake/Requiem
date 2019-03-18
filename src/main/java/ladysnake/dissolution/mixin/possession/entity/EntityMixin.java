package ladysnake.dissolution.mixin.possession.entity;

import ladysnake.dissolution.api.v1.internal.ProtoPossessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin implements ProtoPossessable {

    /* * * * * * * * * * * * * * * * *
      ProtoPossessable implementation
     * * * * * * * * * * * * * * * * */

    @Nullable
    @Override
    public PlayerEntity getPossessor() {
        return null;
    }

    @Override
    public boolean isBeingPossessed() {
        return false;
    }

    /* * * * * * *
      Injections
     * * * * * * */

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = this.getPossessor();
        if (player != null && (player.isCreative() || player == source.getAttacker())) {
            cir.setReturnValue(source.doesDamageToCreative());
        }
    }

    @Inject(method = "canUsePortals", at = @At("HEAD"), cancellable = true)
    private void canUsePortals(CallbackInfoReturnable<Boolean> cir) {
        if (this.isBeingPossessed()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void startRiding(Entity mount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = this.getPossessor();
        if (player != null) {
            cir.setReturnValue(player.startRiding(mount));
        }
    }
}
