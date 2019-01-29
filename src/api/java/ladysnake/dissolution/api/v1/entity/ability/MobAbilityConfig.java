package ladysnake.dissolution.api.v1.entity.ability;

import net.minecraft.entity.mob.MobEntity;

/**
 * A {@link MobAbilityConfig} is used to configure a {@link MobAbilityController}
 *
 * @param <E> The type of entities for which this config can be applied
 */
public interface MobAbilityConfig<E extends MobEntity> {
    DirectAbility<? super E> getDirectAbility(E mob, AbilityType type);

    IndirectAbility<? super E> getIndirectAbility(E mob, AbilityType type);
}
