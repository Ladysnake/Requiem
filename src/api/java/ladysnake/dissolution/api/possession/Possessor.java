package ladysnake.dissolution.api.possession;

import net.minecraft.entity.mob.MobEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public interface Possessor {
    boolean startPossessing(MobEntity mob);

    boolean canStartPossessing(MobEntity mob);

    void stopPossessing();

    @Nullable Possessable getPossessedEntity();

    @Nullable UUID getPossessedEntityUuid();

    default boolean isPossessing() {
        return getPossessedEntityUuid() != null;
    }
}
