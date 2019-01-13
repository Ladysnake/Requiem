package ladysnake.dissolution.common.entity.ai.attribute;

import ladysnake.reflectivefabric.reflection.typed.TypedGetter;
import ladysnake.reflectivefabric.reflection.typed.TypedMethodHandles;
import net.minecraft.entity.attribute.*;

import java.util.Map;

public final class AttributeHelper {
    private AttributeHelper() { throw new AssertionError(); }
    private static TypedGetter<AbstractEntityAttributeContainer, Map> abstractEntityAttributeContainer$attributes =
            TypedMethodHandles.findGetter(AbstractEntityAttributeContainer.class, "field_111154_a", Map.class);
    private static TypedGetter<AbstractEntityAttributeContainer, Map> abstractEntityAttributeContainer$attributesByName =
            TypedMethodHandles.findGetter(AbstractEntityAttributeContainer.class, "field_111153_b", Map.class);
    private static TypedGetter<EntityAttributeContainer, Map> entityAttributeContainer$instancesByName =
            TypedMethodHandles.findGetter(EntityAttributeContainer.class, "field_111163_c", Map.class);

    public static void substituteAttributeInstance(AbstractEntityAttributeContainer EntityAttributeContainer, EntityAttributeInstance replacement) {
        @SuppressWarnings("unchecked")
        final Map<EntityAttribute, EntityAttributeInstance> attributes = abstractEntityAttributeContainer$attributes.invoke(EntityAttributeContainer);
        @SuppressWarnings("unchecked")
        final Map<String, EntityAttributeInstance> attributesByName = abstractEntityAttributeContainer$attributesByName.invoke(EntityAttributeContainer);
        @SuppressWarnings("unchecked")
        final Map<String, EntityAttributeInstance> instancesByName = (EntityAttributeContainer instanceof EntityAttributeContainer)
                ? entityAttributeContainer$instancesByName.invoke((EntityAttributeContainer) EntityAttributeContainer)
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
