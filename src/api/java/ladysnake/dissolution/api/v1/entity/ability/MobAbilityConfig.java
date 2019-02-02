package ladysnake.dissolution.api.v1.entity.ability;

import ladysnake.dissolution.api.v1.internal.ApiInternals;
import net.minecraft.entity.mob.MobEntity;

import java.util.function.Function;

/**
 * A {@link MobAbilityConfig} is used to configure a {@link MobAbilityController}
 *
 * @param <E> The type of entities for which this config can be applied
 */
public interface MobAbilityConfig<E extends MobEntity> {
    DirectAbility<? super E> getDirectAbility(E mob, AbilityType type);

    IndirectAbility<? super E> getIndirectAbility(E mob, AbilityType type);

    static <T extends MobEntity> Builder<T> builder() {
        return ApiInternals.mobAbilityConfig$builderImpl();
    }

    interface Builder<E extends MobEntity> {
        default Builder<E> directAttack(Function<E, DirectAbility<? super E>> factory) {
            return direct(AbilityType.ATTACK, factory);
        }

        default Builder<E> directInteract(Function<E, DirectAbility<? super E>> factory) {
            return direct(AbilityType.INTERACT, factory);
        }

        default Builder<E> indirectAttack(Function<E, IndirectAbility<? super E>> factory) {
            return indirect(AbilityType.ATTACK, factory);
        }

        default Builder<E> indirectInteract(Function<E, IndirectAbility<? super E>> factory) {
            return indirect(AbilityType.INTERACT, factory);
        }

        Builder<E> direct(AbilityType type, Function<E, DirectAbility<? super E>> factory);

        Builder<E> indirect(AbilityType type, Function<E, IndirectAbility<? super E>> factory);

        MobAbilityConfig<E> build();
    }
}
