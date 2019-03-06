package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.v1.possession.conversion.PossessableConverterProvider;
import ladysnake.dissolution.api.v1.possession.conversion.PossessableSubstitutionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

public class LazyDefaultPossessionConversionRegistry extends SimplePossessionConversionRegistry {

    private final PossessableConverterProvider defaultConverterProvider;

    public LazyDefaultPossessionConversionRegistry(PossessableConverterProvider defaultConverterProvider) {
        this.defaultConverterProvider = defaultConverterProvider;
    }

    @Override
    public boolean canBePossessed(EntityType<?> entityType) {
        return isAllowed(entityType);
    }

    @Override
    protected <T extends MobEntity> PossessableSubstitutionHandler<T> getConverterFor(EntityType<?> entityType, Class<? extends MobEntity> entityClass) {
        @SuppressWarnings("unchecked")
        EntityType<T> mobType = (EntityType<T>) entityType;
        @SuppressWarnings("unchecked")
        Class<T> mobClass = (Class<T>) entityClass;
        if (!isEntityRegistered(mobType)) {
            PossessableSubstitutionHandler<T> substitutionHandler = defaultConverterProvider.get(mobType, mobClass);
            if (substitutionHandler != null) {
                this.registerPossessedConverter(mobType, substitutionHandler);
            }
        }
        return super.getConverterFor(entityType, entityClass);
    }
}
