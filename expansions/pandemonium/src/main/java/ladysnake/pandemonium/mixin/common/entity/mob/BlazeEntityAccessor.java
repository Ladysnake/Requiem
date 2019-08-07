package ladysnake.pandemonium.mixin.common.entity.mob;

import net.minecraft.entity.mob.BlazeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlazeEntity.class)
public interface BlazeEntityAccessor {
    @Invoker
    boolean invokeIsFireActive();

    @Invoker
    void invokeSetFireActive(boolean active);
}
