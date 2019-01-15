package ladysnake.dissolution.api;

import ladysnake.dissolution.api.possession.Possessor;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.CheckForNull;

/**
 * Implemented by {@link PlayerEntity players}.
 */
public interface DissolutionPlayer extends Possessor {

    /**
     * @return the player's handler, or <code>null</code> if the player is not a remnant
     */
    @CheckForNull
    RemnantHandler getRemnantHandler();

    void setRemnantHandler(@CheckForNull RemnantHandler handler);

    default boolean isRemnant() {
        return this.getRemnantHandler() != null;
    }
}
