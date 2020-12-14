package ladysnake.requiem.common.impl.remnant;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class WandererRemnantState implements RemnantState {
    private final PlayerEntity player;

    public WandererRemnantState(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean isIncorporeal() {
        return !PossessionComponent.get(this.player).isPossessing();
    }

    @Override
    public boolean isVagrant() {
        return true;
    }

    @Override
    public boolean setVagrant(boolean vagrant) {
        return vagrant; // becoming vagrant always succeeds, merging always fails
    }

    @Override
    public boolean canDissociateFrom(MobEntity possessed) {
        return true;
    }

    @Override
    public void prepareRespawn(ServerPlayerEntity original, boolean lossless) {
        // NO-OP
    }

    @Override
    public void serverTick() {
        MobEntity possessedEntity = PossessionComponent.get(this.player).getPossessedEntity();
        // TODO randomly transfer attrition
    }
}
