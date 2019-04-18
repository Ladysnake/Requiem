package ladysnake.requiem.common.impl.ability;

import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.common.entity.ability.RangedAttackAbility;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.RangedAttacker;
import net.minecraft.entity.mob.MobEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DefaultedMobAbilityRegistry implements MobAbilityRegistry {
    private final Map<EntityType<? extends MobEntity>, MobAbilityConfig<?>> configs = new HashMap<>();
    private final MobAbilityConfig<MobEntity> defaultConfig;

    public DefaultedMobAbilityRegistry(MobAbilityConfig<MobEntity> defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends MobEntity> MobAbilityConfig<? super E> getConfig(E entity) {
        EntityType<E> entityType = (EntityType<E>) entity.getType();
        MobAbilityConfig<? super E> registered = getRegisteredConfig(entityType);
        if (registered == null) {
            MobAbilityConfig.Builder builder = MobAbilityConfig.builder();
            if (entity instanceof RangedAttacker) {
                builder.indirectAttack(e -> new RangedAttackAbility<>((MobEntity & RangedAttacker)e));
            }
            return builder.build();
        }
        return registered;
    }

    @Override
    public <E extends MobEntity> MobAbilityConfig<? super E> getConfig(EntityType<E> entityType) {
        MobAbilityConfig<? super E> registered = getRegisteredConfig(entityType);
        if (registered == null) {
            return defaultConfig;
        }
        return registered;
    }

    @Nullable
    private <E extends MobEntity> MobAbilityConfig<? super E> getRegisteredConfig(EntityType<?> entityType) {
        @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"}) MobAbilityConfig<E> ret = (MobAbilityConfig<E>) this.configs.get(entityType);
        return ret;
    }

    @Override
    public <E extends MobEntity> void register(EntityType<E> entityType, MobAbilityConfig<? super E> config) {
        this.configs.put(entityType, config);
    }

}
