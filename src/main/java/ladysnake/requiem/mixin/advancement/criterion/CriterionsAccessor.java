package ladysnake.requiem.mixin.advancement.criterion;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.Criterions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Criterions.class)
public interface CriterionsAccessor {
    @SuppressWarnings("PublicStaticMixinMember")    // Not true for invokers
    @Invoker
    static <T extends Criterion<?>> T invokeRegister(T criterion) {
        return criterion;
    }
}
