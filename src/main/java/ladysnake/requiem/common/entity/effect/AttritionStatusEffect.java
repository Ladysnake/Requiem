package ladysnake.requiem.common.entity.effect;

import ladysnake.requiem.Requiem;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.Identifier;

public class AttritionStatusEffect extends StatusEffect {
    public static final Identifier ATTRITION_BACKGROUND = Requiem.id("textures/gui/attrition_background.png");

    public AttritionStatusEffect(StatusEffectType type, int color) {
        super(type, color);
    }

    @Override
    public double method_5563(int amplifier, EntityAttributeModifier entityAttributeModifier) {
        return super.method_5563(Math.min(amplifier, 3), entityAttributeModifier);
    }
}
