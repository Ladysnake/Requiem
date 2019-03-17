package ladysnake.dissolution.api.v1;

import ladysnake.dissolution.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.dissolution.api.v1.remnant.RemnantType;
import net.minecraft.util.registry.Registry;

/**
 * A {@link DissolutionPlugin} is an entry point for API consumers.
 *
 * @author Pyrofab
 * @see DissolutionApi#registerPlugin(DissolutionPlugin)
 */
public interface DissolutionPlugin {
    /**
     * Called when dissolution's core features have been fully initialized.
     * <p>
     * This method is called before any other {@code DissolutionPlugin} method.
     */
    default void onDissolutionInitialize() {}

    /**
     * Register custom {@link ladysnake.dissolution.api.v1.entity.ability.MobAbility mob abilities}
     * for known entity types.
     * <p>
     * The passed in {@link MobAbilityRegistry} can be safely reused outside of this method.
     * Stored instances should be refreshed each time this method is called.
     *
     * @param registry Dissolution's ability registry
     */
    default void registerMobAbilities(MobAbilityRegistry registry) {}

    /**
     * Register {@link RemnantType} to provide custom {@link ladysnake.dissolution.api.v1.remnant.RemnantState} players
     * can be in.
     * <p>
     * The passed in {@link Registry} can be safely reused outside of this method.
     * Stored instances should be refreshed each time this method is called.
     *
     * @param registry Dissolution's remnant type registry
     */
    default void registerRemnantStates(Registry<RemnantType> registry) {}
}
