package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.v1.possession.conversion.PossessableConverterProvider;
import ladysnake.dissolution.api.v1.possession.conversion.PossessableSubstitutionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

public class LazyDefaultPossessionRegistry extends SimplePossessionRegistry {

    private PossessableConverterProvider defaultConverterProvider;

    public LazyDefaultPossessionRegistry(PossessableConverterProvider defaultConverterProvider) {
        this.defaultConverterProvider = defaultConverterProvider;
    }

    @Override
    public boolean canBePossessed(EntityType<?> entityType) {
        return !isBlacklisted(entityType);
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
