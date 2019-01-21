package ladysnake.dissolution.api.v1.possession;

import ladysnake.dissolution.api.v1.entity.TriggerableAttacker;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * A {@link Possessable} entity can be possessed by a player through a {@link PossessionManager}.
 * When possessed, the entity should stop acting on its own, and act as a delegate body
 * for the possessing player.
 */
public interface Possessable extends TriggerableAttacker {
    /**
     * Returns an {@link Optional} describing the {@link UUID unique id} of the
     * player possessing this entity, or an empty {@code Optional} if this
     * entity is not being possessed.
     *
     * @return an {@code Optional} describing the UUID of the possessor player
     */
    Optional<UUID> getPossessorUuid();

    /**
     * Returns an {@link Optional} describing the {@link PlayerEntity}
     * possessing this entity, or an empty {@code Optional} if there is
     * no player possessing this entity.
     * <p>
     * This method can return an empty {@code Optional} if the player
     * associated with the {@link #getPossessorUuid() possessor uuid}
     * cannot be found in the world this entity is in.
     *
     * @return an {@code Optional} describing the possessor player
     */
    Optional<PlayerEntity> getPossessor();

    /**
     * Returns whether this entity has a defined {@link #getPossessorUuid() possessor}.
     *
     * @return {@code true} if this entity has a defined possessor, otherwise {@code false}
     */
    default boolean isBeingPossessed() {
        return this.getPossessorUuid().isPresent();
    }

    /**
     * Returns whether this entity is in a state ready to be possessed by the given player.
     *
     * @param player the {@link PlayerEntity} wishing to initiate the possession
     * @return {@code true} if this entity can be possessed by the given player, otherwise {@code false}
     * @implNote The default implementation checks whether it has no current possessor
     */
    boolean canBePossessedBy(PlayerEntity player);

    /**
     * Sets the player possessing this entity.
     *
     * @param possessor the new possessor of this entity
     */
    void setPossessor(@Nullable PlayerEntity possessor);
}
