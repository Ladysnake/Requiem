package ladysnake.requiem.common.entity.effect;

import ladysnake.requiem.Requiem;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.registry.Registry;

public final class RequiemStatusEffects {
    public static final StatusEffect ATTRITION = createEffect(StatusEffectType.HARMFUL, 0xAA3322);

    public static void init() {
        registerEffect(ATTRITION, "attrition");
    }

    public static StatusEffect createEffect(StatusEffectType type, int color) {
        return new StatusEffect(type, color) {};
    }

    public static void registerEffect(StatusEffect effect, String name) {
        Registry.register(Registry.STATUS_EFFECT, Requiem.id(name), effect);
    }
}
