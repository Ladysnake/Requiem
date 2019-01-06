package ladysnake.dissolution.common.entity.ai.attribute;

import ladysnake.dissolution.lib.reflection.typed.TypedGetter;
import ladysnake.dissolution.lib.reflection.typed.TypedReflection;
import net.minecraft.entity.attribute.*;

import java.util.Map;

public final class AttributeHelper {
    private AttributeHelper() { throw new AssertionError(); }
    private static TypedGetter<AbstractEntityAttributeContainer, Map> AbstractEntityAttributeContainer$attributes =
            TypedReflection.findGetter(AbstractEntityAttributeContainer.class, "field_111154_a", Map.class);
    private static TypedGetter<AbstractEntityAttributeContainer, Map> AbstractEntityAttributeContainer$attributesByName =
            TypedReflection.findGetter(AbstractEntityAttributeContainer.class, "field_111153_b", Map.class);
    private static TypedGetter<EntityAttributeContainer, Map> EntityAttributeContainer$instancesByName =
            TypedReflection.findGetter(EntityAttributeContainer.class, "field_111163_c", Map.class);

    public static void substituteAttributeInstance(AbstractEntityAttributeContainer EntityAttributeContainer, EntityAttributeInstance replacement) {
        @SuppressWarnings("unchecked")
        final Map<EntityAttribute, EntityAttributeInstance> attributes = AbstractEntityAttributeContainer$attributes.invoke(EntityAttributeContainer);
        @SuppressWarnings("unchecked")
        final Map<String, EntityAttributeInstance> attributesByName = AbstractEntityAttributeContainer$attributesByName.invoke(EntityAttributeContainer);
        @SuppressWarnings("unchecked")
        final Map<String, EntityAttributeInstance> instancesByName = (EntityAttributeContainer instanceof EntityAttributeContainer)
                ? EntityAttributeContainer$instancesByName.invoke((EntityAttributeContainer) EntityAttributeContainer)
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
