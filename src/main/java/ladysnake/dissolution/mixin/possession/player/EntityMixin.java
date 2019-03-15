package ladysnake.dissolution.mixin.possession.player;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getMaxBreath", at = @At("HEAD"), cancellable = true)
    private void delegateMaxBreath(CallbackInfoReturnable<Integer> cir) {
        if (this instanceof DissolutionPlayer) {
            PossessionComponent possessionComponent = ((DissolutionPlayer) this).getPossessionComponent();
            // This method can be called in the constructor
            //noinspection ConstantConditions
            if (possessionComponent != null) {
                Entity possessedEntity = (Entity) possessionComponent.getPossessedEntity();
                if (possessedEntity != null) {
                    cir.setReturnValue(possessedEntity.getMaxBreath());
                }
            }
        }
    }

    @Inject(method = "getBreath", at = @At("HEAD"), cancellable = true)
    private void delegateBreath(CallbackInfoReturnable<Integer> cir) {
        if (this instanceof DissolutionPlayer) {
            PossessionComponent possessionComponent = ((DissolutionPlayer) this).getPossessionComponent();
            // This method can be called in the constructor
            //noinspection ConstantConditions
            if (possessionComponent != null) {
                Entity possessedEntity = (Entity) ((DissolutionPlayer) this).getPossessionComponent().getPossessedEntity();
                if (possessedEntity != null) {
                    cir.setReturnValue(possessedEntity.getBreath());
                }
            }
        }
    }
}
