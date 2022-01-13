/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common;

import io.github.ladysnake.locki.Locki;
import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.internal.ApiInternals;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.ability.DefaultedMobAbilityRegistry;
import ladysnake.requiem.core.ability.ImmutableMobAbilityConfig;
import ladysnake.requiem.core.data.CommonSubDataManagerHelper;
import ladysnake.requiem.core.data.ServerSubDataManagerHelper;
import ladysnake.requiem.core.inventory.PlayerInventoryLimiter;
import ladysnake.requiem.core.movement.MovementAltererManager;
import ladysnake.requiem.core.remnant.SoulbindingRegistryImpl;
import ladysnake.requiem.core.util.reflection.ReflectionHelper;
import ladysnake.requiem.core.util.reflection.UncheckedReflectionException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;
import org.apiguardian.api.API;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public final class ApiInitializer {

    public static void init() {
        try {
            ReflectionHelper.<Supplier<MobAbilityConfig.Builder<?>>>setField(ApiInternals.class.getDeclaredField("abilityBuilderFactory"),
                ImmutableMobAbilityConfig.Builder::new);
            ReflectionHelper.<SubDataManagerHelper>setField(ApiInternals.class.getDeclaredField("serverSubDataManagerHelper"),
                new ServerSubDataManagerHelper());
            ReflectionHelper.<SubDataManagerHelper>setField(ApiInternals.class.getDeclaredField("clientSubDataManagerHelper"),
                new CommonSubDataManagerHelper());
            ReflectionHelper.<InventoryLimiter>setField(ApiInternals.class.getDeclaredField("inventoryLimiter"),
                new PlayerInventoryLimiter(Locki.registerLock(RequiemCore.id("inventory_limiter"), false)));
            ReflectionHelper.<MobAbilityRegistry>setField(ApiInternals.class.getDeclaredField("mobAbilityRegistry"),
                new DefaultedMobAbilityRegistry(ImmutableMobAbilityConfig.DEFAULT));
            ReflectionHelper.<SoulbindingRegistry>setField(ApiInternals.class.getDeclaredField("soulbindingRegistry"),
                new SoulbindingRegistryImpl());
            initSubDataManagers();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            RequiemCore.LOGGER.error("Could not initialize the mod's API");
            throw new UncheckedReflectionException(e);
        }
    }

    private static void initSubDataManagers() throws IllegalAccessException, NoSuchFieldException {
        initSubDataManager(MovementAltererManager::new, ApiInternals.class.getDeclaredField("movementRegistryGetter"));
    }

    private static <T extends SubDataManager<?>> void initSubDataManager(Supplier<T> factory, Field target) throws IllegalAccessException {
        T serverManager = factory.get();
        T clientManager = factory.get();
        SubDataManagerHelper.getServerHelper().registerSubDataManager(serverManager);
        SubDataManagerHelper.getClientHelper().registerSubDataManager(clientManager);
        ReflectionHelper.<Function<World, T>>setField(
            target,
            w -> w == null || !w.isClient ? serverManager : clientManager
        );
    }

    public static void discoverEntryPoints() {
        FabricLoader.getInstance()
            .getEntrypoints(RequiemApi.ENTRYPOINT_KEY, RequiemPlugin.class)
            .forEach(RequiemApi::registerPlugin);
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
