package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.v1.possession.conversion.PossessableConverterProvider;
import ladysnake.dissolution.api.v1.possession.conversion.PossessableSubstitutionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

public class LazyDefaultPossessionConversionRegistry extends SimplePossessionConversionRegistry {

    private PossessableConverterProvider defaultConverterProvider;

    public LazyDefaultPossessionConversionRegistry(PossessableConverterProvider defaultConverterProvider) {
        this.defaultConverterProvider = defaultConverterProvider;
    }

    @Override
    public boolean canBePossessed(EntityType<?> entityType) {
        return isAllowed(entityType);
    }

    @Override
    protected <T extends MobEntity> PossessableSubstitutionHandler<T> getConverterFor(EntityType<?> entityType) {
        @SuppressWarnings("unchecked")
        EntityType<T> mobType = (EntityType<T>) entityType;
        if (!isEntityRegistered(mobType)) {
            PossessableSubstitutionHandler<T> substitutionHandler = defaultConverterProvider.get(mobType);
            if (substitutionHandler != null) {
                this.registerPossessedConverter(mobType, substitutionHandler);
            }
        }
        return super.getConverterFor(entityType);
    }
}
