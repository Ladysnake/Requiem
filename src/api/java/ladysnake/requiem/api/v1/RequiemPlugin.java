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
package ladysnake.requiem.api.v1;

import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.minecraft.util.registry.Registry;

/**
 * A {@link RequiemPlugin} is an entry point for API consumers.
 *
 * @author Pyrofab
 * @see RequiemApi#registerPlugin(RequiemPlugin)
 */
public interface RequiemPlugin {
    /**
     * Called when requiem's core features have been fully initialized.
     * <p>
     * This method is called before any other {@code RequiemPlugin} method.
     */
    default void onRequiemInitialize() {}

    /**
     * Register custom {@link ladysnake.requiem.api.v1.entity.ability.MobAbility mob abilities}
     * for known entity types.
     * <p>
     * The passed in {@link MobAbilityRegistry} can be safely reused outside of this method.
     * Stored instances should be refreshed each time this method is called.
     *
     * @param registry Requiem's ability registry
     */
    default void registerMobAbilities(MobAbilityRegistry registry) {}

    /**
     * Register {@link RemnantType} to provide custom {@link ladysnake.requiem.api.v1.remnant.RemnantState} players
     * can be in.
     * <p>
     * The passed in {@link Registry} can be safely reused outside of this method.
     * Stored instances should be refreshed each time this method is called.
     *
     * @param registry Requiem's remnant type registry
     */
    default void registerRemnantStates(Registry<RemnantType> registry) {}
}
