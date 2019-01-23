package ladysnake.dissolution.mixin.entity;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(
            method = "setSize",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0
            ),
            cancellable = true
    )
    public void overrideSoulResize(float width, float height, CallbackInfo info) {
        if (this instanceof DissolutionPlayer) {
            Entity possessed = (Entity) ((DissolutionPlayer)this).getPossessionManager().getPossessedEntity();
            if (possessed != null && (possessed.getWidth() != width || possessed.getHeight() != height)) {
                this.setSize(possessed.getWidth(), possessed.getHeight());
                info.cancel();
            }
        }
    }

    @Shadow protected abstract void setSize(float float_1, float float_2);
}
