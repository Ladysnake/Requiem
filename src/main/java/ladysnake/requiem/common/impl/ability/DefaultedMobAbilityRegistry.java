/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DefaultedMobAbilityRegistry implements MobAbilityRegistry {
    private final Map<EntityType<? extends MobEntity>, MobAbilityConfig<?>> configs = new HashMap<>();
    private final MobAbilityConfig<MobEntity> defaultConfig;

    public DefaultedMobAbilityRegistry(MobAbilityConfig<MobEntity> defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends MobEntity> MobAbilityConfig<? super E> getConfig(E entity) {
        EntityType<E> entityType = (EntityType<E>) entity.getType();
        MobAbilityConfig<? super E> registered = getRegisteredConfig(entityType);
        if (registered == null) {
            return MobAbilityConfig.builder().build();
        }
        return registered;
    }

    @Override
    public <E extends MobEntity> MobAbilityConfig<? super E> getConfig(EntityType<E> entityType) {
        MobAbilityConfig<? super E> registered = getRegisteredConfig(entityType);
        if (registered == null) {
            return defaultConfig;
        }
        return registered;
    }

    @Nullable
    private <E extends MobEntity> MobAbilityConfig<? super E> getRegisteredConfig(EntityType<?> entityType) {
        @SuppressWarnings("unchecked") MobAbilityConfig<E> ret = (MobAbilityConfig<E>) this.configs.get(entityType);
        return ret;
    }

    @Override
    public <E extends MobEntity> void register(EntityType<E> entityType, MobAbilityConfig<? super E> config) {
        this.configs.put(entityType, config);
    }

}
