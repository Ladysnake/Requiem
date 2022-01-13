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
package ladysnake.requiem.api.v1.internal;

import com.google.common.collect.ImmutableSet;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.annotation.AccessedThroughReflection;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.MovementRegistry;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * Internal methods used by other API classes to interact actively with the API provider.
 * This class should never be referenced directly by an API consumer.
 */
@API(status = INTERNAL)
public final class ApiInternals {

    private ApiInternals() { throw new AssertionError(); }

    /**
     * The set of all registered plugins.
     */
    private static final Set<RequiemPlugin> plugins = new HashSet<>();
    /**
     * Called whenever a plugin is registered.
     * When the API provider gets initialized, is replaced by a handler
     * that also performs plugin initialization.
     */
    @AccessedThroughReflection
    private static Consumer<RequiemPlugin> registerHandler = plugin -> {
        if (!plugins.add(plugin)) {
            throw new IllegalStateException(plugin + " has been registered twice!");
        }
    };

    @AccessedThroughReflection
    private static Supplier<MobAbilityConfig.Builder<?>> abilityBuilderFactory;
    @AccessedThroughReflection
    private static SubDataManagerHelper clientSubDataManagerHelper;
    @AccessedThroughReflection
    private static SubDataManagerHelper serverSubDataManagerHelper;
    @AccessedThroughReflection
    private static InventoryLimiter inventoryLimiter;
    @AccessedThroughReflection
    private static MobAbilityRegistry mobAbilityRegistry;
    @AccessedThroughReflection
    private static SoulbindingRegistry soulbindingRegistry;
    @AccessedThroughReflection
    private static Function<@Nullable World, MovementRegistry> movementRegistryGetter;

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> MobAbilityConfig.Builder<T> mobAbilityConfig$builderImpl() {
        if (abilityBuilderFactory == null) throw new UninitializedApiException("MobAbilityConfig Builder is not available");
        return (MobAbilityConfig.Builder<T>) abilityBuilderFactory.get();
    }

    /**
     * Registers a plugin object. This method is thread-safe.
     * @param entryPoint a previously unregistered plugin object
     */
    public static void registerPluginInternal(RequiemPlugin entryPoint) {
        synchronized (plugins) {
            registerHandler.accept(entryPoint);
        }
    }

    /**
     * Creates a read-only copy of {@link #plugins}. This method is thread-safe.
     * @return an ImmutableSet containing all currently registered plugins.
     */
    public static ImmutableSet<RequiemPlugin> copyRegisteredPlugins() {
        synchronized (plugins) {
            return ImmutableSet.copyOf(plugins);
        }
    }

    public static SubDataManagerHelper getClientSubDataManagerHelper() {
        if (clientSubDataManagerHelper == null) throw new UninitializedApiException("Client SubDataManagerHelper is not available");
        return clientSubDataManagerHelper;
    }

    public static SubDataManagerHelper getServerSubDataManagerHelper() {
        if (serverSubDataManagerHelper == null) throw new UninitializedApiException("Server SubDataManagerHelper is not available");
        return serverSubDataManagerHelper;
    }

    public static MobAbilityRegistry getMobAbilityRegistry() {
        if (mobAbilityRegistry == null) throw new UninitializedApiException("MobAbilityRegistry is not available");
        return mobAbilityRegistry;
    }

    public static SoulbindingRegistry getSoulbindingRegistry() {
        if (soulbindingRegistry == null) throw new UninitializedApiException("SoulboundRegistry is not available");
        return soulbindingRegistry;
    }

    public static MovementRegistry getMovementRegistry(@Nullable World world) {
        if (movementRegistryGetter == null) throw new UninitializedApiException("MovementRegistry is not available");
        return movementRegistryGetter.apply(world);
    }

    public static InventoryLimiter getInventoryLimiter() {
        return inventoryLimiter;
    }
}
