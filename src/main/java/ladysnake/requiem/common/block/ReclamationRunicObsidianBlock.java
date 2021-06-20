package ladysnake.requiem.common.block;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Supplier;

public class ReclamationRunicObsidianBlock extends RunicObsidianBlock {
    public ReclamationRunicObsidianBlock(Settings settings, Supplier<StatusEffect> effect, int maxLevel) {
        super(settings, effect, maxLevel);
    }

    @Override
    public void applyEffect(ServerPlayerEntity target, int runeLevel, int obeliskWidth) {
        if (!target.hasStatusEffect(this.getEffect())) {
            int effectDuration = (10 - obeliskWidth) * 20 * 60;
            target.addStatusEffect(new StatusEffectInstance(this.getEffect(), effectDuration, runeLevel - 1, true, true));
        }
    }
}
