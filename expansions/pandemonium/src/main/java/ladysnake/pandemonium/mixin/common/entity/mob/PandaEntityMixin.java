package ladysnake.pandemonium.mixin.common.entity.mob;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PandaEntity.class)
public abstract class PandaEntityMixin extends AnimalEntity implements Possessable {
    protected PandaEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    protected abstract boolean canEat(ItemStack stack);

    @Shadow
    public abstract void setScared(boolean scared);

    @Shadow
    public abstract boolean isEating();

    @Shadow
    public abstract boolean isScared();

    @Shadow
    private float scaredAnimationProgress;

    @Shadow
    private float lastScaredAnimationProgress;

    @Shadow
    public abstract void setLyingOnBack(boolean lyingOnBack);

    @ModifyArg(method = "updateEatingAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PandaEntity;setEating(Z)V", ordinal = 0))
    private boolean stopEatingConcrete(boolean eat) {
        if (!this.canEat(this.getEquippedStack(EquipmentSlot.MAINHAND))) {
            return false;
        }
        return eat;
    }

    @Override
    public void onPossessorSet(@Nullable PlayerEntity possessor) {
        this.setLyingOnBack(false);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PandaEntity;updateScaredAnimation()V"))
    private void scareIfSneaking(CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            this.setScared(possessor.isSneaking());
        }
    }

    @Inject(method = "updateScaredAnimation", at = @At("RETURN"))
    private void recalculateDimensions(CallbackInfo ci) {
        if (this.lastScaredAnimationProgress != this.scaredAnimationProgress) {
            this.calculateDimensions();
        }
    }

    @Intrinsic  // If someone else redefines this method, just let them do whatever
    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).scaled(1, 1 + this.scaredAnimationProgress * 0.85f);
    }
}
