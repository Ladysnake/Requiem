package ladysnake.dissolution.common.entity.ai.attribute;

import ladylib.reflection.Getter;
import ladylib.reflection.LLReflectionHelper;
import net.minecraft.entity.ai.attributes.*;

import java.util.Map;

public final class AttributeHelper {
    private AttributeHelper() { throw new AssertionError(); }
    private static Getter<AbstractAttributeMap, Map> abstractAttributeMap$attributes =
            LLReflectionHelper.findGetter(AbstractAttributeMap.class, "field_111154_a", Map.class);
    private static Getter<AbstractAttributeMap, Map> abstractAttributeMap$attributesByName =
            LLReflectionHelper.findGetter(AbstractAttributeMap.class, "field_111153_b", Map.class);
    private static Getter<AttributeMap, Map> attributeMap$instancesByName =
            LLReflectionHelper.findGetter(AttributeMap.class, "field_111163_c", Map.class);

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
            String description = ((RangedAttribute) attribute).getDescription();
            if (description != null && instancesByName.containsKey(description)) {
                instancesByName.put(description, replacement);
            }
        }
    }
}
