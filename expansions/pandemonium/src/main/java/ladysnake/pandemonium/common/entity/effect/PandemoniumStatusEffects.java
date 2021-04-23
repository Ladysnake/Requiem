package ladysnake.pandemonium.common.entity.effect;

import ladysnake.pandemonium.Pandemonium;
import ladysnake.requiem.Requiem;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.registry.Registry;

public class PandemoniumStatusEffects {
    public static final StatusEffect PENANCE = new PenanceStatusEffect(StatusEffectType.HARMFUL, 0xAA3322); //TODO change the color

    public static void init() {
        registerEffect(PENANCE, "penance");
    }

    public static void registerEffect(StatusEffect effect, String name) {
        Registry.register(Registry.STATUS_EFFECT, Pandemonium.id(name), effect);
    }
}
