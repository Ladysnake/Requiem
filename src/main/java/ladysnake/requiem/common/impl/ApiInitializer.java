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
package ladysnake.requiem.common.impl;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.internal.ApiInternals;
import ladysnake.requiem.common.impl.ability.ImmutableMobAbilityConfig;
import ladysnake.requiem.common.util.reflection.UncheckedReflectionException;
import org.apiguardian.api.API;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public class ApiInitializer {
    public static void init() {
        try {
            setAbilityBuilderFactory(ImmutableMobAbilityConfig.Builder::new);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Requiem.LOGGER.error("Could not initialize the mod's API");
            throw new UncheckedReflectionException(e);
        }
    }

    public static void setAbilityBuilderFactory(Supplier<MobAbilityConfig.Builder<?>> factory) throws IllegalAccessException, NoSuchFieldException {
        Field f = ApiInternals.class.getDeclaredField("abilityBuilderFactory");
        f.setAccessible(true);
        f.set(null, factory);
    }

    public static void setPluginCallback(Consumer<RequiemPlugin> callback) {
        try {
            Field f = ApiInternals.class.getDeclaredField("registerHandler");
            f.setAccessible(true);
            @SuppressWarnings("unchecked") Consumer<RequiemPlugin> registerHandler = (Consumer<RequiemPlugin>) f.get(null);
            f.set(null, registerHandler.andThen(callback));
            RequiemApi.getRegisteredPlugins().forEach(callback);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new UncheckedReflectionException("Failed to load plugins", e);
        }
    }

}
