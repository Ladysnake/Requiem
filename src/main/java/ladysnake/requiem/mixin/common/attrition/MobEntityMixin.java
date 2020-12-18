package ladysnake.requiem.mixin.common.attrition;

import ladysnake.requiem.api.v1.remnant.AttritionFocus;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Inject(method = "cannotDespawn", at = @At("RETURN"), cancellable = true)
    private void cannotDespawn(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && AttritionFocus.KEY.get(this).hasAttrition()) {
            cir.setReturnValue(true);
        }
    }
}
