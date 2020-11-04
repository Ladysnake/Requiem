package ladysnake.pandemonium.mixin.common.entity.mob;

import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EvokerEntity.class)
public interface EvokerEntityAccessor {
    @Invoker
    void invokeSetWololoTarget(SheepEntity sheep);
}
