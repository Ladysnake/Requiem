package ladysnake.requiem.mixin.common.remnant;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPredicates.class)
public abstract class EntityPredicatesMixin {
    @Dynamic
    @Inject(method = {"method_5910", "method_24517"}, at = @At("RETURN"), cancellable = true)
    private void exceptCreativeOrSpectator(Entity tested, CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValueZ() && RemnantComponent.isSoul(tested)) {
            info.setReturnValue(false);
        }
    }
}
