package ladysnake.requiem.mixin.common.possession.player;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PossessorPlayerEntityMixin extends LivingEntity {
    protected PossessorPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Intrinsic
    @Override
    public int getMaxAir() {
        return super.getMaxAir();
    }

    @Dynamic(mixin = PossessorPlayerEntityMixin.class, value = "Added by the intrinsic above")
    @Inject(method = "getMaxAir", at = @At("HEAD"), cancellable = true)
    private void delegateMaxBreath(CallbackInfoReturnable<Integer> cir) {
        // This method can be called in the constructor, before CCA is initialized
        if (ComponentProvider.fromEntity(this).getComponentContainer() != null) {
            Entity possessedEntity = PossessionComponent.getPossessedEntity(this);
            if (possessedEntity != null) {
                cir.setReturnValue(possessedEntity.getMaxAir());
            }
        }
    }

    @Intrinsic
    @Override
    public boolean isClimbing() {
        return super.isClimbing();
    }

    @Dynamic(mixin = PossessorPlayerEntityMixin.class, value = "Added by the intrinsic above")
    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void canClimb(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && this.horizontalCollision) {
            cir.setReturnValue(MovementAlterer.KEY.get(this).canClimbWalls());
        }
    }

    @Intrinsic
    @Override
    public boolean collides() {
        return super.collides();
    }

    @Dynamic(mixin = PossessorPlayerEntityMixin.class, value = "Added by the intrinsic above")
    @Inject(method = "collides", at = @At("RETURN"), cancellable = true)
    private void preventSoulsCollision(CallbackInfoReturnable<Boolean> info) {
        if (RemnantComponent.isSoul(this)) {
            info.setReturnValue(false);
        }
    }

    @Intrinsic
    @Override
    public boolean canAvoidTraps() {
        return super.canAvoidTraps();
    }

    @Dynamic(mixin = PossessorPlayerEntityMixin.class, value = "Added by the intrinsic above")
    @Inject(method = "canAvoidTraps", at = @At("RETURN"), cancellable = true)
    private void soulsAvoidTraps(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() || RemnantComponent.isIncorporeal(this));
    }

    @Inject(method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
    private void adjustEyeHeight(EntityPose pose, EntityDimensions size, CallbackInfoReturnable<Float> cir) {
        // This method can be called in the Entity constructor, before CCA is initialized
        if (ComponentProvider.fromEntity(this).getComponentContainer() != null) {
            LivingEntity possessed = PossessionComponent.getPossessedEntity(this);
            if (possessed != null) {
                cir.setReturnValue(((LivingEntityAccessor) possessed).invokeGetEyeHeight(pose, possessed.getDimensions(pose)));
            }
        }
    }
}
