package ladysnake.dissolution.api.possession;

import net.minecraft.entity.mob.MobEntity;

import javax.annotation.CheckForNull;
import java.util.UUID;

/**
 * A {@link Possessor} can possess a {@link Possessable} entity, using it as a delegate body.
 */
public interface Possessor {
    boolean startPossessing(MobEntity mob);

    boolean canStartPossessing(MobEntity mob);

    void stopPossessing();

    @CheckForNull Possessable getPossessedEntity();

    @CheckForNull UUID getPossessedEntityUuid();

    default boolean isPossessing() {
        return getPossessedEntityUuid() != null;
    }
}
