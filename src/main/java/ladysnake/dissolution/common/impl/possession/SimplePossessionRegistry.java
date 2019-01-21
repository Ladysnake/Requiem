package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.conversion.PossessableSubstitutionHandler;
import ladysnake.dissolution.api.v1.possession.conversion.PossessionRegistry;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SimplePossessionRegistry implements PossessionRegistry {
    protected final Map<EntityType<? extends MobEntity>, PossessableSubstitutionHandler> converters = new HashMap<>();

    public boolean isBlacklisted(EntityType<?> entityType) {
        return DissolutionEntityTags.POSSESSION_BLACKLIST.contains(entityType);
    }

    @Override
    public <T extends MobEntity> void registerPossessedConverter(EntityType<T> baseEntityType, PossessableSubstitutionHandler<T> possessedEntityType) {
        this.converters.put(baseEntityType, possessedEntityType);
    }

    @Override
    public boolean canBePossessed(EntityType<?> entityType) {
        return !isBlacklisted(entityType) && this.converters.containsKey(entityType);
    }

    @Nullable
    @Override
    public <T extends MobEntity> Possessable convert(T entity, PlayerEntity possessor) {
        if (!this.canBePossessed(entity)) {
            return null;
        }
        PossessableSubstitutionHandler<T> converter = this.getConverterFor(entity.getType());
        if (converter != null) {
            return converter.apply(entity, possessor);
        }
        return null;
    }

    /**
     * Gets the appropriate converter for entities of the given type.
     * <em>Note: the returned substitution handler is unsafely casted and should only be used on the same entity type</em>
     */
    @Nullable
    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    protected <T extends MobEntity> PossessableSubstitutionHandler<T> getConverterFor(EntityType<?> entityType) {
        return (PossessableSubstitutionHandler<T>) this.converters.get(entityType);
    }

    @Override
    public boolean isEntityRegistered(EntityType<? extends MobEntity> entityType) {
        return false;
    }
}
