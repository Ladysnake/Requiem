package ladysnake.dissolution.api.v1;

import ladysnake.dissolution.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.dissolution.api.v1.possession.conversion.PossessionConversionRegistry;

/**
 * A {@link DissolutionPlugin} is an entry point for API consumers.
 *
 * @author Pyrofab
 * @see DissolutionApi#registerPlugin(DissolutionPlugin)
 */
public interface DissolutionPlugin {
    /**
     * Called when dissolution's core features have been fully initialized
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
     * Register {@link ladysnake.dissolution.api.v1.possession.conversion.PossessableSubstitutionHandler custom handlers}
     * to control how an entity supports possession.
     * <p>
     * The passed in {@link PossessionConversionRegistry} can be safely reused outside of this method.
     * Stored instances should be refreshed each time this method is called.
     *
     * @param registry Dissolution's conversion registry
     */
    default void registerPossessedConversions(PossessionConversionRegistry registry) {}
}
