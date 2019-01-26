package ladysnake.dissolution.common.impl.remnant;

import ladysnake.dissolution.api.v1.remnant.RemnantState;
import net.minecraft.nbt.CompoundTag;

public final class NullRemnantState implements RemnantState {

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
    public CompoundTag writeToTag() {
        return new CompoundTag();
    }

    @Override
    public void readFromTag(CompoundTag tag) {
        // NO-OP
    }
}
