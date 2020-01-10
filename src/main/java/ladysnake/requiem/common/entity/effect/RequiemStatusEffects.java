package ladysnake.requiem.common.entity.effect;

import ladysnake.requiem.Requiem;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.registry.Registry;

public final class RequiemStatusEffects {
    public static final StatusEffect ATTRITION = new AttritionStatusEffect(StatusEffectType.HARMFUL, 0xAA3322)
        .addAttributeModifier(EntityAttributes.MAX_HEALTH, "069ae0b1-4014-41dd-932f-a5da4417d711", -0.2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static void init() {
        registerEffect(ATTRITION, "attrition");
    }

    public static void registerEffect(StatusEffect effect, String name) {
        Registry.register(Registry.STATUS_EFFECT, Requiem.id(name), effect);
    }
}
