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
package ladysnake.requiem.api.v1.internal;

import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.annotation.AccessedThroughReflection;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import net.minecraft.entity.mob.MobEntity;
import org.apiguardian.api.API;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public final class ApiInternals {
    private ApiInternals() { throw new AssertionError(); }

    @Nullable
    @AccessedThroughReflection
    private static Supplier<MobAbilityConfig.Builder<?>> abilityBuilderFactory;

    private static final List<RequiemPlugin> plugins = new ArrayList<>();
    @AccessedThroughReflection
    private static Consumer<RequiemPlugin> registerHandler = plugins::add;
    @AccessedThroughReflection
    private static SubDataManagerHelper clientSubDataManagerHelper;
    @AccessedThroughReflection
    private static SubDataManagerHelper serverSubDataManagerHelper;

    @SuppressWarnings("unchecked")
    public static <T extends MobEntity> MobAbilityConfig.Builder<T> mobAbilityConfig$builderImpl() {
        if (abilityBuilderFactory == null) {
            throw new UninitializedApiException();
        }
        return (MobAbilityConfig.Builder<T>) abilityBuilderFactory.get();
    }

    public static void registerPluginInternal(RequiemPlugin entryPoint) {
        registerHandler.accept(entryPoint);
    }

    public static Stream<RequiemPlugin> streamRegisteredPlugins() {
        return plugins.stream();
    }

    public static SubDataManagerHelper getClientSubDataManagerHelper() {
        return clientSubDataManagerHelper;
    }

    public static SubDataManagerHelper getServerSubDataManagerHelper() {
        return serverSubDataManagerHelper;
    }
}
