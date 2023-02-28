package ladysnake.requiem.core.mixin.possession.possessed;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(RevengeGoal.class)
public abstract class RevengeGoalMixin extends TrackTargetGoal {
    public RevengeGoalMixin(MobEntity mob, boolean checkVisibility) {
        super(mob, checkVisibility);
    }

    @ModifyArg(method = "callSameTypeForRevenge", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesByClass(Ljava/lang/Class;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private Predicate<Entity> stopCallingPossessedMobs(Predicate<Entity> basePredicate) {
        // mobs should not be called to target themselves
        // but maybe some mod out there relies on that, so only apply the fix to possessed mobs
        // Zombified piglins get a pass because funny achievement
        return basePredicate.and(e -> this.mob.getAttacker() != e || !((Possessable)e).isBeingPossessed() || this.mob instanceof ZombifiedPiglinEntity);
    }
}
