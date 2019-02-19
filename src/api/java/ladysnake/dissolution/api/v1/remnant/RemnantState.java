package ladysnake.dissolution.api.v1.remnant;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public interface RemnantState {
    String NULL_STATE_ID = "dissolution:mortal";

    boolean isIncorporeal();

    boolean isSoul();

    void setSoul(boolean incorporeal);

    void fracture();

    RemnantType getType();

    CompoundTag toTag(CompoundTag tag);

    void fromTag(CompoundTag tag);

    /**
     * A predicate matching entities that are remnant
     */
    Predicate<Entity> REMNANT = e -> e instanceof DissolutionPlayer && ((DissolutionPlayer) e).isRemnant();

    /**
     * Helper method to get the remnant state of an entity if it exists
     *
     * @param entity a possibly remnant entity
     * @return the remnant handler of that entity
     */
    static Optional<RemnantState> getIfRemnant(@Nullable Entity entity) {
        if (REMNANT.test(entity)) {
            //The predicate guarantees that the entity is not null
            //noinspection ConstantConditions
            return Optional.of(((DissolutionPlayer) entity).getRemnantState());
        }
        return Optional.empty();
    }

}
