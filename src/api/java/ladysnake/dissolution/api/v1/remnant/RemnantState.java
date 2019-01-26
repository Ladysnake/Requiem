package ladysnake.dissolution.api.v1.remnant;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Optional;

public interface RemnantState {

    boolean isIncorporeal();

    boolean isSoul();

    void setSoul(boolean incorporeal);

    CompoundTag writeToTag();

    void readFromTag(CompoundTag tag);

    /**
     * Helper method to get the remnant state of an entity if it exists
     *
     * @param entity a possibly remnant entity
     * @return the remnant handler of that entity
     */
    static Optional<RemnantState> getIfRemnant(@Nullable Entity entity) {
        if (entity instanceof DissolutionPlayer && ((DissolutionPlayer) entity).isRemnant()) {
            return Optional.of(((DissolutionPlayer) entity).getRemnantState());
        }
        return Optional.empty();
    }

}
