package ladysnake.dissolution.api.v1.internal;

import ladysnake.dissolution.api.v1.annotation.AccessedThroughReflection;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityConfig;
import net.minecraft.entity.mob.MobEntity;
import org.apiguardian.api.API;

import java.util.function.Supplier;

import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
public final class ApiInternals {
    @AccessedThroughReflection
    private static Supplier<MobAbilityConfig.Builder<?>> abilityBuilderFactory;

    @SuppressWarnings("unchecked")
    public static <T extends MobEntity> MobAbilityConfig.Builder<T> mobAbilityConfig$builderImpl() {
        if (abilityBuilderFactory == null) {
            throw new UninitializedApiException();
        }
        return (MobAbilityConfig.Builder<T>) ApiInternals.abilityBuilderFactory.get();
    }
}
