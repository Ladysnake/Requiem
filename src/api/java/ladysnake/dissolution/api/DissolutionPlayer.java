package ladysnake.dissolution.api;

import ladysnake.dissolution.api.remnant.RemnantHandler;
import org.jetbrains.annotations.Nullable;

public interface DissolutionPlayer {

    @Nullable
    RemnantHandler getRemnantHandler();

    void setRemnantHandler(RemnantHandler handler);

    default boolean isRemnant() {
        return this.getRemnantHandler() != null;
    }
}
