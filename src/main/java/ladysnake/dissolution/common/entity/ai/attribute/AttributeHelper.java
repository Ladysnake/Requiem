package ladysnake.dissolution.common.entity.ai.attribute;

import ladysnake.dissolution.mixin.entity.attribute.AbstractEntityAttributeContainerAccessorMixin;
import ladysnake.dissolution.mixin.entity.attribute.EntityAttributeContainerAccessorMixin;
import net.minecraft.entity.attribute.*;

import java.util.Map;

public final class AttributeHelper {
    private AttributeHelper() { throw new AssertionError(); }

    public static void substituteAttributeInstance(AbstractEntityAttributeContainer entityAttributeContainer, EntityAttributeInstance replacement) {
        final Map<EntityAttribute, EntityAttributeInstance> attributes = ((AbstractEntityAttributeContainerAccessorMixin)entityAttributeContainer).getInstancesByKey();
        final Map<String, EntityAttributeInstance> attributesByName = ((AbstractEntityAttributeContainerAccessorMixin)entityAttributeContainer).getInstancesById();
        final Map<String, EntityAttributeInstance> instancesByName = (entityAttributeContainer instanceof EntityAttributeContainer)
                ? ((EntityAttributeContainerAccessorMixin) entityAttributeContainer).getInstancesByName()
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
