package ladysnake.requiem.api.v1.entity.ability;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

public interface MobAbilityRegistry {

    <E extends MobEntity> MobAbilityConfig<? super E> getConfig(E entity);

    <E extends MobEntity> MobAbilityConfig<? super E> getConfig(EntityType<E> entityType);

    <E extends MobEntity> void register(EntityType<E> entityType, MobAbilityConfig<? super E> config);
}
