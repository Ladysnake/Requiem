package ladysnake.pandemonium.common.entity.effect;

import ladysnake.pandemonium.Pandemonium;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.registry.Registry;

public class PandemoniumStatusEffects {
    public static final StatusEffect SOUL_WEBBED = new SoulWebbedEffect(StatusEffectType.HARMFUL, 0xEFEFEF)
        .addAttributeModifier(EntityAttributes.MOVEMENT_SPEED, "d125fc82-2fd1-4dea-ac92-1355e15ecdfc", -0.15, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final StatusEffect BODY_RECALL = new BodyRecallEffect(StatusEffectType.NEUTRAL, 0xAA22BB);

    public static void init() {
        register("soul_webbed", SOUL_WEBBED);
        register("body_recall", BODY_RECALL);
    }

    private static void register(String id, StatusEffect effect) {
        Registry.register(Registry.STATUS_EFFECT, Pandemonium.id(id), effect);
    }
}
