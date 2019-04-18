package ladysnake.requiem.api.v1;

import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Implemented by {@link PlayerEntity players}.
 */
public interface RequiemPlayer {

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
