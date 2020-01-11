package ladysnake.requiem.common.entity.effect;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class AttritionStatusEffect extends StatusEffect {
    public static final Identifier ATTRITION_BACKGROUND = Requiem.id("textures/gui/attrition_background.png");
    public static final DamageSource ATTRITION_HARDCORE_DEATH = new DamageSource("requiem.attrition") {{
        // We need this dirty anonymous initializer because everything is protected
        this.setBypassesArmor();
        this.setOutOfWorld();
    }};

    public static void apply(PlayerEntity target) {
        StatusEffectInstance attrition = target.getStatusEffect(RequiemStatusEffects.ATTRITION);
        int amplifier = attrition == null ? 0 : attrition.getAmplifier() + 1;
        if (amplifier <= 3) {
            target.addStatusEffect(new StatusEffectInstance(
                RequiemStatusEffects.ATTRITION,
                300,
                amplifier,
                false,
                false,
                true
            ));
        } else {
            if (target.world.getLevelProperties().isHardcore()) {
                RequiemPlayer.from(target).become(RemnantTypes.MORTAL);
                target.damage(ATTRITION_HARDCORE_DEATH, Float.MAX_VALUE);
            }
        }
    }

    public AttritionStatusEffect(StatusEffectType type, int color) {
        super(type, color);
    }

    @Override
    public double method_5563(int amplifier, EntityAttributeModifier entityAttributeModifier) {
        return super.method_5563(Math.min(amplifier, 3), entityAttributeModifier);
    }
}
