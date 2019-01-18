package ladysnake.dissolution.api;

import ladysnake.dissolution.api.possession.PossessionManager;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Implemented by {@link PlayerEntity players}.
 */
public interface DissolutionPlayer {

    /**
     * @return the player's handler, or {@code null} if the player is not a remnant
     */
    @Nullable
    RemnantHandler getRemnantHandler();

    PossessionManager getPossessionManager();

    void setRemnantHandler(@CheckForNull RemnantHandler handler);

    default boolean isRemnant() {
        return this.getRemnantHandler() != null;
    }
}
