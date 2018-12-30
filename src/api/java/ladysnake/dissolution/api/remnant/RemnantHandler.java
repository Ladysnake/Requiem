package ladysnake.dissolution.api.remnant;

import ladysnake.dissolution.api.DissolutionPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public interface RemnantHandler {

    /**
     * Helper method to get the impl capability of an entity if it exists
     * @param entity a possibly impl entity
     * @return the impl capability of that entity
     */
    static Optional<RemnantHandler> get(Entity entity) {
        if (entity instanceof DissolutionPlayer) {
            return Optional.ofNullable(((DissolutionPlayer)entity).getRemnantHandler());
        }
        return Optional.empty();
    }

    boolean isIncorporeal();

    void setIncorporeal(boolean incorporeal);

    CompoundTag writeToTag();

    void readFromTag(CompoundTag tag);

}
