package ladysnake.pandemonium.mixin.client.render;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.entity.mob.ShulkerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerEntityModel.class)
public abstract class ShulkerEntityModelMixin {
    @Shadow
    @Final
    private ModelPart head;
    @Unique
    private boolean disabledNerdFace;

    @Inject(method = "setAngles", at = @At("RETURN"))
    private void removeNerdFace(ShulkerEntity shulkerEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (((Possessable)shulkerEntity).isBeingPossessed()) {
            this.head.visible = false;
            this.disabledNerdFace = true;
        } else if (disabledNerdFace) {  // probably slightly incompatible, but who's going to do the same thing ?!
            this.head.visible = true;
        }
    }
}
