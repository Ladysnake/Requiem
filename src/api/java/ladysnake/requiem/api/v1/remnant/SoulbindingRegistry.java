package ladysnake.requiem.api.v1.remnant;

import ladysnake.requiem.api.v1.internal.ApiInternals;
import net.minecraft.entity.effect.StatusEffect;

public interface SoulbindingRegistry {
    static SoulbindingRegistry instance() {
        return ApiInternals.getSoulbindingRegistry();
    }

    /**
     * Registers a {@code StatusEffect} as soulbound.
     * @see #isSoulbound(StatusEffect)
     */
    void registerSoulbound(StatusEffect effect);

    /**
     * Returns {@code true} if the given {@link StatusEffect} is registered as
     * soulbound.
     *
     * <p> Soulbound status effects are carried over when the player dies,
     * and stay with the soul when leaving a body. Their updates are also paused
     * while the player is in soul form, and they cannot be cleared by anything
     * except the /clear command. Finally, they have custom backgrounds in the
     * inventory and in-game hud.
     *
     * @param effect status effect to test
     * @return {@code true} if {@code effect} is soulbound
     * @see #registerSoulbound(StatusEffect)
     */
    boolean isSoulbound(StatusEffect effect);
}
