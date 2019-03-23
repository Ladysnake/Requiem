package ladysnake.dissolution.api.v1.possession;

import ladysnake.dissolution.api.v1.entity.ability.MobAbilityController;
import ladysnake.dissolution.api.v1.internal.ProtoPossessable;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * A {@link Possessable} entity can be possessed by a player through a {@link PossessionComponent}.
 * When possessed, the entity should stop acting on its own, and act as a delegate body
 * for the possessing player.
 * <p>
 * All methods in this interface have defaults so subclasses can omit implementations.
 * Those default implementations are actually dummies, actual default implementations
 * are provided by {@link net.minecraft.entity.LivingEntity}.
 */
public interface Possessable extends ProtoPossessable {

    /**
     * Returns an {@link Optional} describing the {@link UUID unique id} of the
     * player possessing this entity, or an empty {@code Optional} if this
     * entity is not being possessed.
     *
     * @return an {@code Optional} describing the UUID of the possessor player
     */
    default Optional<UUID> getPossessorUuid() { return Optional.empty(); }

    /**
     * Returns the {@link PlayerEntity} currently possessing this entity,
     * or {@code null} if there is no player possessing this entity.
     * <p>
     * This method can return {@code null} if the player
     * associated with the {@link #getPossessorUuid() possessor uuid}
     * cannot be found in the world this entity is in.
     *
     * @return the player currently possessing this entity.
     */
    @Nullable
    @Override
    default PlayerEntity getPossessor() { return null; }

    /**
     * Returns whether this entity has a defined {@link #getPossessorUuid() possessor}.
     *
     * @return {@code true} if this entity has a defined possessor, otherwise {@code false}
     */
    @Override
    default boolean isBeingPossessed() { return false; }

    /**
     * Returns whether this entity is in a state ready to be possessed by the given player.
     *
     * @param player the {@link PlayerEntity} wishing to initiate the possession
     * @return {@code true} if this entity can be possessed by the given player, otherwise {@code false}
     * @implNote The default implementation checks whether it has no current possessor and it is alive
     */
    default boolean canBePossessedBy(PlayerEntity player) { return true; }

    default MobAbilityController getMobAbilityController() { return MobAbilityController.DUMMY; }

    /**
     * Sets the player possessing this entity.
     *
     * @param possessor the new possessor of this entity
     */
    default void setPossessor(@Nullable PlayerEntity possessor) {}

}
