package ladysnake.dissolution.api.possession;

import ladysnake.dissolution.api.entity.TriggerableAttacker;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * A {@link Possessable} entity can be possessed by a {@link PossessionManager}.
 * When possessed, the entity should stop acting on its own, and act as a delegate body
 * for the possessing player.
 */
public interface Possessable extends TriggerableAttacker {
    Optional<UUID> getPossessingEntityUuid();

    Optional<PlayerEntity> getPossessingEntity();

    default boolean isBeingPossessed() {
        return this.getPossessingEntityUuid().isPresent();
    }

    boolean canBePossessedBy(PlayerEntity player);

    void setPossessingEntity(@Nullable UUID possessingEntity);
}
