package ladysnake.requiem.mixin.common.remnant;

import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.common.util.ExtendedShapeContext;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityShapeContext.class)
public abstract class EntityShapeContextMixin implements ExtendedShapeContext {
    @Unique
    private boolean noClipping;

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/entity/Entity;)V")
    private void setEntityField(Entity entity, CallbackInfo info) {
        this.noClipping = entity instanceof PlayerEntity && MovementAlterer.KEY.get(entity).isNoClipping();
    }

    @Override
    public boolean requiem_isNoClipping() {
        return this.noClipping;
    }
}
