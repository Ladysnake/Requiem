package ladysnake.dissolution.api.v1.internal;

import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;

public interface ProtoPossessable {
    @Nullable
    PlayerEntity getPossessor();

    boolean isBeingPossessed();
}
