package ladysnake.dissolution.api.v1.remnant;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public interface RemnantHandler {

    /**
     * Helper method to get the remnant handler of an entity if it exists
     * @param entity a possibly remnant entity
     * @return the remnant handler of that entity
     */
    static Optional<RemnantHandler> get(Entity entity) {
        if (entity instanceof DissolutionPlayer) {
            return Optional.ofNullable(((DissolutionPlayer)entity).getRemnantHandler());
        }
        return Optional.empty();
    }

    boolean isIncorporeal();

    boolean isSoul();

    void setSoul(boolean incorporeal);

    CompoundTag writeToTag();

    void readFromTag(CompoundTag tag);

}
