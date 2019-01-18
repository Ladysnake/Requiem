package ladysnake.dissolution.api.possession;

import net.minecraft.entity.mob.MobEntity;

import javax.annotation.CheckForNull;
import java.util.UUID;

/**
 * A {@link PossessionManager} handles a player's possession status.
 */
public interface PossessionManager {
    boolean startPossessing(MobEntity mob);

    boolean canStartPossessing(MobEntity mob);

    void stopPossessing();

    @CheckForNull Possessable getPossessedEntity();

    @CheckForNull UUID getPossessedEntityUuid();

    default boolean isPossessing() {
        return getPossessedEntityUuid() != null;
    }
}
