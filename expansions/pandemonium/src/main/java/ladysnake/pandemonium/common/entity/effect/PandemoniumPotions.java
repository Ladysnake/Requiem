package ladysnake.pandemonium.common.entity.effect;

import ladysnake.pandemonium.Pandemonium;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;

public final class PandemoniumPotions {
    public static final Potion BODY_RECALL = new Potion("pandemonium.body_recall", new StatusEffectInstance(PandemoniumStatusEffects.BODY_RECALL, 200));

    public static void init() {
        register("body_recall", BODY_RECALL);
    }

    private static void register(String id, Potion potion) {
        Registry.register(Registry.POTION, Pandemonium.id(id), potion);
    }
}
