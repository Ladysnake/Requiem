package ladysnake.dissolution.common.entity.ai.attribute;

import ladylib.reflection.TypedReflection;
import ladylib.reflection.typed.TypedGetter;
import net.minecraft.entity.ai.attributes.*;

import java.util.Map;

public final class AttributeHelper {
    private AttributeHelper() { throw new AssertionError(); }
    private static TypedGetter<AbstractAttributeMap, Map> abstractAttributeMap$attributes =
            TypedReflection.findGetter(AbstractAttributeMap.class, "field_111154_a", Map.class);
    private static TypedGetter<AbstractAttributeMap, Map> abstractAttributeMap$attributesByName =
            TypedReflection.findGetter(AbstractAttributeMap.class, "field_111153_b", Map.class);
    private static TypedGetter<AttributeMap, Map> attributeMap$instancesByName =
            TypedReflection.findGetter(AttributeMap.class, "field_111163_c", Map.class);

    public static void substituteAttributeInstance(AbstractAttributeMap attributeMap, IAttributeInstance replacement) {
        @SuppressWarnings("unchecked")
        final Map<IAttribute, IAttributeInstance> attributes = abstractAttributeMap$attributes.invoke(attributeMap);
        @SuppressWarnings("unchecked")
        final Map<String, IAttributeInstance> attributesByName = abstractAttributeMap$attributesByName.invoke(attributeMap);
        @SuppressWarnings("unchecked")
        final Map<String, IAttributeInstance> instancesByName = (attributeMap instanceof AttributeMap)
                    ? attributeMap$instancesByName.invoke((AttributeMap) attributeMap)
                    : null;
        IAttribute attribute = replacement.getAttribute();
        String name = attribute.getName();
        attributes.put(attribute, replacement);
        attributesByName.put(name, replacement);
        if (instancesByName != null) {
            String description;
            if (attribute instanceof RangedAttribute) {
                description = ((RangedAttribute) attribute).getDescription();
            } else {
                description = null;
            }
            if (description != null && instancesByName.containsKey(description)) {
                instancesByName.put(description, replacement);
            }
        }
    }
}
