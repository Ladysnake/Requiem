package ladysnake.dissolution.common.impl;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionApi;
import ladysnake.dissolution.api.v1.DissolutionPlugin;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.dissolution.api.v1.internal.ApiInternals;
import ladysnake.dissolution.common.impl.ability.SimpleMobAbilityConfig;
import ladysnake.reflectivefabric.reflection.UncheckedReflectionException;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ApiInitializer {
    public static void init() {
        try {
            setAbilityBuilderFactory(SimpleMobAbilityConfig.Builder::new);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Dissolution.LOGGER.error("Could not initialize the mod's API");
            throw new UncheckedReflectionException(e);
        }
    }

    public static void setAbilityBuilderFactory(Supplier<MobAbilityConfig.Builder<?>> factory) throws IllegalAccessException, NoSuchFieldException {
        Field f = ApiInternals.class.getDeclaredField("abilityBuilderFactory");
        f.setAccessible(true);
        f.set(null, factory);
    }

    public static void setPluginCallback(Consumer<DissolutionPlugin> callback) {
        try {
            Field f = DissolutionApi.class.getDeclaredField("registerHandler");
            f.setAccessible(true);
            @SuppressWarnings("unchecked") Consumer<DissolutionPlugin> registerHandler = (Consumer<DissolutionPlugin>) f.get(null);
            f.set(null, registerHandler.andThen(callback));
            DissolutionApi.getRegisteredPlugins().forEach(callback);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new UncheckedReflectionException("Failed to load plugins", e);
        }
    }

}
