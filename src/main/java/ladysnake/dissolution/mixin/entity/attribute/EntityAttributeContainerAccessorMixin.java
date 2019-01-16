package ladysnake.dissolution.mixin.entity.attribute;

import net.minecraft.entity.attribute.EntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityAttributeContainer.class)
public interface EntityAttributeContainerAccessorMixin {
    @Accessor
    Map<String, EntityAttributeInstance> getInstancesByName();
}
