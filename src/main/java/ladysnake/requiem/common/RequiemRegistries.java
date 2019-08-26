/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.common;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.impl.ability.DefaultedMobAbilityRegistry;
import ladysnake.requiem.common.impl.ability.ImmutableMobAbilityConfig;
import ladysnake.requiem.common.remnant.RemnantTypes;
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
public final class RequiemRegistries {
    public static final MobAbilityRegistry ABILITIES = new DefaultedMobAbilityRegistry(ImmutableMobAbilityConfig.DEFAULT);
    public static final DefaultedRegistry<RemnantType> REMNANT_STATES = new DefaultedRegistry<>(RemnantState.NULL_STATE_ID);

    public static void init() {
        Registry.register(Registry.REGISTRIES, Requiem.id("remnant_states"), REMNANT_STATES);
        Registry.register(REMNANT_STATES, new Identifier(RemnantState.NULL_STATE_ID), RemnantTypes.MORTAL);
    }

}