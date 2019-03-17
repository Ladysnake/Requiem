package ladysnake.dissolution.mixin.entity.attribute;

import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AbstractEntityAttributeContainer.class)
public interface AbstractEntityAttributeContainerAccessor {
    @Accessor
    Map<EntityAttribute, EntityAttributeInstance> getInstancesByKey();
    @Accessor
    Map<String, EntityAttributeInstance> getInstancesById();
}
