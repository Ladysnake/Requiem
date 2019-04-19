package ladysnake.requiem.mixin.entity.mob;

import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EndermanEntity.class)
public interface EndermanEntityAccessor {
    @SuppressWarnings("UnusedReturnValue")
    @Invoker
    boolean invokeTeleportRandomly();
}
