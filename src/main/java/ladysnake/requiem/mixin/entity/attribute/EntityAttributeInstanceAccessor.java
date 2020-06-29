package ladysnake.requiem.mixin.entity.attribute;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;

@Mixin(EntityAttributeInstance.class)
public interface EntityAttributeInstanceAccessor {
    @Accessor
    Consumer<EntityAttributeInstance> getUpdateCallback();
}
