package ladysnake.dissolution.mixin.entity.ai.goal;

import ladysnake.dissolution.common.entity.PlayerShellEntity;
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
    @Shadow @Nullable protected LivingEntity field_6644;

    @Shadow protected TargetPredicate field_6642;

    @Shadow protected abstract BoundingBox getSearchBox(double double_1);

    public FollowTargetGoalMixin(MobEntity mobEntity_1, boolean boolean_1) {
        super(mobEntity_1, boolean_1);
    }

    @Inject(
            method = "method_18415",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/entity/ai/goal/FollowTargetGoal;field_6644:Lnet/minecraft/entity/LivingEntity;",
                    ordinal = 1,
                    shift = AFTER
            )
    )
    private void addShellsAsTargets(CallbackInfo ci) {
        if (this.field_6644 == null) {
            this.field_6644 = this.entity.world.method_18465(PlayerShellEntity.class, this.field_6642, this.entity, this.entity.x, this.entity.y + (double)this.entity.getStandingEyeHeight(), this.entity.z, this.getSearchBox(this.getFollowRange()));
        }
    }
}
