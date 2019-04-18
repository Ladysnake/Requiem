package ladysnake.requiem.common.entity.ai.attribute;

import ladysnake.requiem.mixin.entity.attribute.AbstractEntityAttributeContainerAccessor;
import ladysnake.requiem.mixin.entity.attribute.EntityAttributeContainerAccessor;
import net.minecraft.entity.attribute.*;

import java.util.Map;

public final class AttributeHelper {
    private AttributeHelper() { throw new AssertionError(); }

    public static void substituteAttributeInstance(AbstractEntityAttributeContainer entityAttributeContainer, EntityAttributeInstance replacement) {
        final Map<EntityAttribute, EntityAttributeInstance> attributes = ((AbstractEntityAttributeContainerAccessor)entityAttributeContainer).getInstancesByKey();
        final Map<String, EntityAttributeInstance> attributesByName = ((AbstractEntityAttributeContainerAccessor)entityAttributeContainer).getInstancesById();
        final Map<String, EntityAttributeInstance> instancesByName = (entityAttributeContainer instanceof EntityAttributeContainer)
                ? ((EntityAttributeContainerAccessor) entityAttributeContainer).getInstancesByName()
                : null;
        EntityAttribute attribute = replacement.getAttribute();
        String name = attribute.getId();
        attributes.put(attribute, replacement);
        attributesByName.put(name, replacement);
        if (instancesByName != null) {
            String description;
            if (attribute instanceof ClampedEntityAttribute) {
                description = ((ClampedEntityAttribute) attribute).getName();
            } else {
                description = null;
            }
            if (description != null && instancesByName.containsKey(description)) {
                instancesByName.put(description, replacement);
            }
        }
    }
}
