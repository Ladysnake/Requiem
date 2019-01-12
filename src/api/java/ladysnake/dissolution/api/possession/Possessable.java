package ladysnake.dissolution.api.possession;

import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public interface Possessable {
    Optional<UUID> getPossessingEntityUuid();

    Optional<PlayerEntity> getPossessingEntity();

    default boolean isBeingPossessed() {
        return this.getPossessingEntityUuid().isPresent();
    }

    boolean canBePossessedBy(PlayerEntity player);

    void setPossessingEntity(@Nullable UUID possessingEntity);
}
