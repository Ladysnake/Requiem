package ladysnake.requiem.api.v1.internal;

import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Collection;

public interface StatusEffectReapplicator {
    Collection<StatusEffectInstance> getReappliedStatusEffects();
}
