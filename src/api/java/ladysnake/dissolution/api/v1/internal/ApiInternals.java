package ladysnake.dissolution.api.v1.internal;

import ladysnake.dissolution.api.v1.DissolutionPlugin;
import ladysnake.dissolution.api.v1.annotation.AccessedThroughReflection;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityConfig;
import net.minecraft.entity.mob.MobEntity;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public final class ApiInternals {
    @AccessedThroughReflection
    private static Supplier<MobAbilityConfig.Builder<?>> abilityBuilderFactory;

    private static List<DissolutionPlugin> plugins = new ArrayList<>();
    @AccessedThroughReflection
    private static Consumer<DissolutionPlugin> registerHandler = plugins::add;

    @SuppressWarnings("unchecked")
    public static <T extends MobEntity> MobAbilityConfig.Builder<T> mobAbilityConfig$builderImpl() {
        if (abilityBuilderFactory == null) {
            throw new UninitializedApiException();
        }
        return (MobAbilityConfig.Builder<T>) ApiInternals.abilityBuilderFactory.get();
    }

    public static void registerPluginInternal(DissolutionPlugin entryPoint) {
        registerHandler.accept(entryPoint);
    }

    public static Stream<DissolutionPlugin> streamRegisteredPlugins() {
        return plugins.stream();
    }
}
