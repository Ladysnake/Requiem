package ladysnake.pandemonium.mixin.common.entity.mob;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("STUCK_ARROW_COUNT")
    static TrackedData<Integer> getStuckArrowCountTrackedData() {
        throw new IllegalStateException();
    }
    @Accessor("STINGER_COUNT")
    static TrackedData<Integer> getStuckStingerCountTrackedData() {
        throw new IllegalStateException();
    }
}
