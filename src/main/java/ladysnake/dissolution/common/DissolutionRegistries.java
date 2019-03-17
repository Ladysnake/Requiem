package ladysnake.dissolution.common;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.api.v1.remnant.RemnantType;
import ladysnake.dissolution.common.impl.ability.DefaultedMobAbilityRegistry;
import ladysnake.dissolution.common.impl.ability.SimpleMobAbilityConfig;
import ladysnake.dissolution.common.impl.remnant.NullRemnantState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Entry point for the possession mechanic.
 * Everything here is subject to be moved to a more specialized place.
 */
@API(status = EXPERIMENTAL)
public final class DissolutionRegistries {
    public static final MobAbilityRegistry ABILITIES = new DefaultedMobAbilityRegistry(SimpleMobAbilityConfig.DEFAULT);
    public static final DefaultedRegistry<RemnantType> REMNANT_STATES = new DefaultedRegistry<>(RemnantState.NULL_STATE_ID);

    public static void init() {
        Registry.register(Registry.REGISTRIES, Dissolution.id("remnant_states"), REMNANT_STATES);
        Registry.register(REMNANT_STATES, new Identifier(RemnantState.NULL_STATE_ID), p -> NullRemnantState.NULL_STATE);
    }

}