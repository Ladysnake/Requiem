package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.possession.Possessable;
import ladysnake.dissolution.api.possession.PossessableSubstitutionHandler;
import ladysnake.dissolution.api.possession.PossessionRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public class SimplePossessionRegistry implements PossessionRegistry {
    protected final Map<EntityType<? extends MobEntity>, PossessableSubstitutionHandler> converters = new HashMap<>();
    protected final Set<EntityType<?>> blacklist = new HashSet<>();

    @Override
    public void addToBlacklist(EntityType<?>... entityTypes) {
        Collections.addAll(blacklist, entityTypes);
    }

    @Override
    public void removeFromBlacklist(EntityType<?>... entityTypes) {
        for (EntityType<?> blacklisted : entityTypes) {
            this.blacklist.remove(blacklisted);
        }
    }

    @Override
    public <T extends MobEntity> void registerPossessedConverter(EntityType<T> baseEntityType, PossessableSubstitutionHandler<T> possessedEntityType) {
        this.converters.put(baseEntityType, possessedEntityType);
    }

    @Override
    public boolean canBePossessed(EntityType<?> entityType) {
        //noinspection SuspiciousMethodCalls
        return !this.blacklist.contains(entityType) && this.converters.containsKey(entityType);
    }

    @Override
    public Possessable convert(MobEntity entity, PlayerEntity possessor) {
        if (!this.canBePossessed(entity)) {
            throw new IllegalArgumentException(entity + " can not be turned into a possessable variant!");
        }
        return this.getConverterFor(entity.getType()).apply(entity, possessor);
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    protected PossessableSubstitutionHandler<MobEntity> getConverterFor(EntityType<?> entityType) {
        return (PossessableSubstitutionHandler<MobEntity>) this.converters.get(entityType);
    }

    @Override
    public boolean isEntityRegistered(EntityType<? extends MobEntity> entityType) {
        return false;
    }
}
