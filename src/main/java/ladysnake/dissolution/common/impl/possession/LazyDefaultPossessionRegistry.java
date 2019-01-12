package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.possession.PossessableSubstitutionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

import java.util.function.Function;

public class LazyDefaultPossessionRegistry extends SimplePossessionRegistry {

    private Function<EntityType<? extends MobEntity>, PossessableSubstitutionHandler<?>> defaultConverterProvider;

    public LazyDefaultPossessionRegistry(Function<EntityType<? extends MobEntity>, PossessableSubstitutionHandler<?>> defaultConverterProvider) {
        this.defaultConverterProvider = defaultConverterProvider;
    }

    @Override
    public boolean canBePossessed(EntityType<?> entityType) {
        return !this.blacklist.contains(entityType);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected PossessableSubstitutionHandler<MobEntity> getConverterFor(EntityType<?> entityType) {
        EntityType<? extends MobEntity> mobType = (EntityType<? extends MobEntity>) entityType;
        if (!isEntityRegistered(mobType)) {
            this.registerPossessedConverter(mobType, (PossessableSubstitutionHandler) defaultConverterProvider.apply(mobType));
        }
        return super.getConverterFor(entityType);
    }
}
