package ladysnake.requiem.mixin.entity.ai.goal;

import ladysnake.requiem.common.entity.PlayerShellEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BoundingBox;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(FollowTargetGoal.class)
public abstract class FollowTargetGoalMixin extends TrackTargetGoal {
    @Shadow @Nullable protected LivingEntity targetEntity;

    @Shadow protected TargetPredicate targetPredicate;

    @Shadow protected abstract BoundingBox getSearchBox(double double_1);

    public FollowTargetGoalMixin(MobEntity mobEntity_1, boolean boolean_1) {
        super(mobEntity_1, boolean_1);
    }

    @Inject(
            method = "findClosestTarget",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/entity/ai/goal/FollowTargetGoal;targetEntity:Lnet/minecraft/entity/LivingEntity;",
                    ordinal = 1,
                    shift = AFTER
            )
    )
    private void addShellsAsTargets(CallbackInfo ci) {
        if (this.targetEntity == null) {
            this.targetEntity = this.entity.world.getClosestEntity(PlayerShellEntity.class, this.targetPredicate, this.entity, this.entity.x, this.entity.y + (double)this.entity.getStandingEyeHeight(), this.entity.z, this.getSearchBox(this.getFollowRange()));
        }
    }
}
