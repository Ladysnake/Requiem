package ladysnake.requiem.api.v1.internal;

import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.annotation.AccessedThroughReflection;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
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
    @Nullable
    @AccessedThroughReflection
    private static Supplier<MobAbilityConfig.Builder<?>> abilityBuilderFactory;

    private static final List<RequiemPlugin> plugins = new ArrayList<>();
    @AccessedThroughReflection
    private static Consumer<RequiemPlugin> registerHandler = plugins::add;

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
}
