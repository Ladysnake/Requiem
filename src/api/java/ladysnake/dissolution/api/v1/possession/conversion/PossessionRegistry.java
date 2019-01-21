package ladysnake.dissolution.api.v1.possession.conversion;

import ladysnake.dissolution.api.v1.possession.Possessable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;

/**
 * A registry handling conversion from standard entities to {@link Possessable} ones.
 */
public interface PossessionRegistry {

    /**
     * Use this method to register your own possessable version of an entity.
     * <p>
     * When an entity is possessed by the player, the mod will try to replace it with an equivalent
     * entity implementing {@link Possessable}. If no converter for that entity is available
     * at the time of that check, no new entity will be instantiated and the player will be unable
     * to possess it. <br>
     * It is recommended that possessed versions inherit from their regular counterparts.
     * </p>
     * <p>
     * Note that entities already implementing {@link Possessable} will not be converted and will rather
     * be possessed directly.
     * </p>
     * This method can be called at any time.<br>
     * Subsequent calls to this method will override the previous ones.
     *
     * @param baseEntityType      the EntityType of the regular entity
     * @param possessedEntityType the EntityType of an equivalent entity that can be possessed
     */
    <E extends MobEntity> void registerPossessedConverter(EntityType<E> baseEntityType, PossessableSubstitutionHandler<E> possessedEntityType);

    /**
     * Checks if an entity type is eligible for possession. <br>
     * This check may not account for technical limitations causing {@link #convert(MobEntity, PlayerEntity)}
     * to return <code>null</code>.
     *
     * @param entityType an entity type
     * @return true if entities of that type can be {@link #convert(MobEntity, PlayerEntity) converted}
     */
    boolean canBePossessed(EntityType<?> entityType);

    /**
     * Checks if an entity is eligible for possession. <br>
     * This check may not account for technical limitations causing {@link #convert(MobEntity, PlayerEntity)}
     * to return <code>null</code>
     *
     * @param entity an entity
     * @return true if the entity can be {@link #convert(MobEntity, PlayerEntity) converted}
     */
    default boolean canBePossessed(MobEntity entity) {
        return this.canBePossessed(entity.getType());
    }

    /**
     * Provides a possessable version of the given entity. <p>
     * The return value will be <code>null</code> if the entity can not be possessed.
     * This method is allowed to return <code>null</code> even if the entity is theoretically
     * possessable. <br>
     * The behaviour of this method is undefined if <code>entity</code> is already Possessable.
     *
     * @param entity the entity to convert
     * @param possessor the player initiating the possession
     * @return a possessable entity behaving similarly to <code>entity</code>
     * @see #canBePossessed(MobEntity)
     */
    @Nullable
    <T extends MobEntity> Possessable convert(T entity, PlayerEntity possessor);

    /**
     * Checks if a call to {@link #registerPossessedConverter(EntityType, PossessableSubstitutionHandler)} has been made for the given
     * <code>entityEntityType</code>. <br>
     * The associated possessed entity EntityType may still be <code>null</code>, the method just returns false
     * if there is no information regarding the given entity EntityType.
     *
     * @param entityType a EntityType of entity
     * @return true if the given EntityType has been registered
     */
    boolean isEntityRegistered(EntityType<? extends MobEntity> entityType);

}
