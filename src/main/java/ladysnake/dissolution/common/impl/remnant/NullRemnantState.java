package ladysnake.dissolution.common.impl.remnant;

import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.api.v1.remnant.RemnantType;
import ladysnake.dissolution.common.remnant.RemnantStates;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NullRemnantState implements RemnantState {

    public static final RemnantState NULL_STATE = new NullRemnantState();

    @Override
    public boolean isIncorporeal() {
        return false;
    }

    @Override
    public boolean isSoul() {
        return false;
    }

    @Override
    public void setSoul(boolean incorporeal) {
        // NO-OP
    }

    @Override
    public void fracture() {
        // NO-OP
    }

    @Override
    public RemnantType getType() {
        return RemnantStates.MORTAL;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        // NO-OP
    }

    @Override
    public void onPlayerClone(ServerPlayerEntity clone, boolean dead) {
        // NO-OP
    }
}
