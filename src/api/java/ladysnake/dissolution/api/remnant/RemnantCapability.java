package ladysnake.dissolution.api.remnant;

import ladysnake.dissolution.api.DissolutionPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public interface RemnantCapability {

    /**
     * Helper method to get the remnant capability of an entity if it exists
     * @param entity a possibly remnant entity
     * @return the remnant capability of that entity
     */
    static Optional<RemnantCapability> get(Entity entity) {
        if (entity instanceof DissolutionPlayer) {
            return Optional.ofNullable(((DissolutionPlayer)entity).getRemnantCapability());
        }
        return Optional.empty();
    }

    boolean isIncorporeal();

    void setIncorporeal(boolean incorporeal);

    CompoundTag writeToTag();

    void readFromTag(CompoundTag tag);

}
