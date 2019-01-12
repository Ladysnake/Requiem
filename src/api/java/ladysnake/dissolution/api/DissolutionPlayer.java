package ladysnake.dissolution.api;

import ladysnake.dissolution.api.possession.Possessor;
import ladysnake.dissolution.api.remnant.RemnantHandler;

import javax.annotation.Nullable;

public interface DissolutionPlayer extends Possessor {

    @Nullable
    RemnantHandler getRemnantHandler();

    void setRemnantHandler(RemnantHandler handler);

    default boolean isRemnant() {
        return this.getRemnantHandler() != null;
    }
}
