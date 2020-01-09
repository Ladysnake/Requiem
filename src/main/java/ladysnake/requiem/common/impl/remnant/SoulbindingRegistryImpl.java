package ladysnake.requiem.common.impl.remnant;

import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import net.minecraft.entity.effect.StatusEffect;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SoulbindingRegistryImpl implements SoulbindingRegistry {
    private final Set<StatusEffect> soulboundEffects = new LinkedHashSet<>();

    @Override
    public void registerSoulbound(StatusEffect effect) {
        soulboundEffects.add(effect);
    }

    @Override
    public boolean isSoulbound(StatusEffect effect) {
        return soulboundEffects.contains(effect);
    }
}
