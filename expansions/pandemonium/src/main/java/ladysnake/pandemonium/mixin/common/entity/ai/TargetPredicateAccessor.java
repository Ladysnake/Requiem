package ladysnake.pandemonium.mixin.common.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(TargetPredicate.class)
public interface TargetPredicateAccessor {
    @Accessor
    Predicate<LivingEntity> getPredicate();
}
