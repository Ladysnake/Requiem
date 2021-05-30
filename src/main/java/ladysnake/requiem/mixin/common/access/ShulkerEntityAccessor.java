package ladysnake.requiem.mixin.common.access;

import net.minecraft.entity.mob.ShulkerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShulkerEntity.class)
public interface ShulkerEntityAccessor {
    @Invoker("getPeekAmount")
    int requiem$getPeekAmount();

    @Invoker("setPeekAmount")
    void requiem$setPeekAmount(int peekAmount);
}
