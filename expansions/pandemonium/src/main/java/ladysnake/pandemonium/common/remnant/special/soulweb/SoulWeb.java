package ladysnake.pandemonium.common.remnant.special.soulweb;

import ladysnake.pandemonium.Pandemonium;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.registry.Registry;

public final class SoulWeb {
    public static final StatusEffect SOUL_WEBBED_EFFECT = new SoulWebbedEffect(StatusEffectType.HARMFUL, 0xEFEFEF)
        .addAttributeModifier(EntityAttributes.MOVEMENT_SPEED, "d125fc82-2fd1-4dea-ac92-1355e15ecdfc", -0.15, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static void init() {
        Registry.register(Registry.STATUS_EFFECT, Pandemonium.id("soul_webbed"), SOUL_WEBBED_EFFECT);
    }
}
