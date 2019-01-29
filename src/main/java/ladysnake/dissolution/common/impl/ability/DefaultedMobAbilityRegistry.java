package ladysnake.dissolution.common.impl.ability;

import ladysnake.dissolution.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

import java.util.HashMap;
import java.util.Map;

public class DefaultedMobAbilityRegistry implements MobAbilityRegistry {
    private final Map<EntityType<? extends MobEntity>, MobAbilityConfig<?>> configs = new HashMap<>();
    private final MobAbilityConfig<MobEntity> defaultConfig;

    public DefaultedMobAbilityRegistry(MobAbilityConfig<MobEntity> defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    @Override
    public <E extends MobEntity> MobAbilityConfig<? super E> getConfig(E entity) {
        @SuppressWarnings("unchecked") EntityType<E> entityType = (EntityType<E>) entity.getType();
        return getConfig(entityType);
    }

    @Override
    public <E extends MobEntity> MobAbilityConfig<? super E> getConfig(EntityType<E> entityType) {
        @SuppressWarnings("unchecked") MobAbilityConfig<? super E> ret = (MobAbilityConfig<E>) this.configs.getOrDefault(entityType, defaultConfig);
        return ret;
    }

    @Override
    public <E extends MobEntity> void register(EntityType<E> entityType, MobAbilityConfig<? super E> config) {
        this.configs.put(entityType, config);
    }

}
