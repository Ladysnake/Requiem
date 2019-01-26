package ladysnake.dissolution.api.v1.possession;

import net.minecraft.entity.mob.MobEntity;

import javax.annotation.CheckForNull;

/**
 * A {@link PossessionComponent} handles a player's possession status.
 */
public interface PossessionComponent {
    boolean startPossessing(MobEntity mob);

    boolean canStartPossessing(MobEntity mob);

    void stopPossessing();

    @CheckForNull Possessable getPossessedEntity();

    boolean isPossessing();
}
