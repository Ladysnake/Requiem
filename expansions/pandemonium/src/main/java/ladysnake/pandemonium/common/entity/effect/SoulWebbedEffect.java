package ladysnake.pandemonium.common.entity.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;

public class SoulWebbedEffect extends StatusEffect {

    public SoulWebbedEffect(StatusEffectType statusEffectType_1, int int_1) {
        super(statusEffectType_1, int_1);
    }

    @Override
    public boolean canApplyUpdateEffect(int time, int potency) {
        return false;
    }
}
