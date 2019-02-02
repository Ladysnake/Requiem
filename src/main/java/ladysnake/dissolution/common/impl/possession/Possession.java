package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.dissolution.api.v1.possession.conversion.PossessionConversionRegistry;
import ladysnake.dissolution.common.impl.ability.DefaultedMobAbilityRegistry;
import ladysnake.dissolution.common.impl.ability.SimpleMobAbilityConfig;
import ladysnake.dissolution.common.impl.possession.asm.AsmConverterProvider;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Entry point for the possession mechanic.
 * Everything here is subject to be moved to a more specialized place.
 */
@API(status = EXPERIMENTAL)
public final class Possession {
    private static PossessionConversionRegistry conversionRegistry = new LazyDefaultPossessionConversionRegistry(new AsmConverterProvider());
    private static MobAbilityRegistry abilityRegistry = new DefaultedMobAbilityRegistry(SimpleMobAbilityConfig.DEFAULT);

    public static PossessionConversionRegistry getConversionRegistry() {
        return conversionRegistry;
    }

    public static MobAbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    public static void init() {

    }

}