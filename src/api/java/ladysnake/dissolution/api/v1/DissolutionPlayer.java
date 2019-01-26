package ladysnake.dissolution.api.v1;

import ladysnake.dissolution.api.v1.entity.MovementAlterer;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Implemented by {@link PlayerEntity players}.
 */
public interface DissolutionPlayer {

    /**
     * @return the player's remnant state
     */
    RemnantState getRemnantState();

    /**
     * @return the player's movement alterer
     */
    MovementAlterer getMovementAlterer();

    /**
     * @return the player's possession component
     */
    PossessionComponent getPossessionComponent();

    void setRemnant(boolean remnant);

    boolean isRemnant();
}
