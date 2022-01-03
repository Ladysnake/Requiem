/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.entity.ability;

import ladysnake.requiem.api.v1.internal.ApiInternals;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

/**
 * A {@link MobAbilityConfig} is used to configure a {@link MobAbilityController}
 *
 * @param <E> The type of entities for which this config can be applied
 */
public interface MobAbilityConfig<E extends LivingEntity> {
    DirectAbility<? super E, ?> getDirectAbility(E mob, AbilityType type);

    IndirectAbility<? super E> getIndirectAbility(E mob, AbilityType type);

    static <T extends LivingEntity> Builder<T> builder() {
        return ApiInternals.mobAbilityConfig$builderImpl();
    }

    interface Builder<E extends LivingEntity> {
        default Builder<E> directAttack(Function<E, DirectAbility<? super E, ?>> factory) {
            return direct(AbilityType.ATTACK, factory);
        }

        default Builder<E> directInteract(Function<E, DirectAbility<? super E, ?>> factory) {
            return direct(AbilityType.INTERACT, factory);
        }

        default Builder<E> indirectAttack(Function<E, IndirectAbility<? super E>> factory) {
            return indirect(AbilityType.ATTACK, factory);
        }

        default Builder<E> indirectInteract(Function<E, IndirectAbility<? super E>> factory) {
            return indirect(AbilityType.INTERACT, factory);
        }

        Builder<E> direct(AbilityType type, Function<E, DirectAbility<? super E, ?>> factory);

        Builder<E> indirect(AbilityType type, Function<E, IndirectAbility<? super E>> factory);

        MobAbilityConfig<E> build();
    }
}
