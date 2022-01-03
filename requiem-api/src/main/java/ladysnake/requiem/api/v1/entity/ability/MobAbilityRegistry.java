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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

public interface MobAbilityRegistry {
    static MobAbilityRegistry instance() {
        return ApiInternals.getMobAbilityRegistry();
    }

    <E extends MobEntity> MobAbilityConfig<? super E> getConfig(E entity);

    <E extends MobEntity> MobAbilityConfig<? super E> getConfig(EntityType<E> entityType);

    <E extends MobEntity> void register(EntityType<E> entityType, MobAbilityConfig<? super E> config);

    /**
     * Begins the registration of a {@link MobAbilityConfig} for the given {@code entityType}.
     *
     * <p>{@code registry.beginRegistration(entityType)...build()} behaves as if:
     * {@code registry.register(entityType, MobAbilityConfig.<>builder()...build())}
     */
    <E extends MobEntity> MobAbilityConfig.Builder<E> beginRegistration(EntityType<E> entityType);
}
