package ladysnake.dissolution.api;

import ladysnake.dissolution.api.remnant.RemnantCapability;
import org.jetbrains.annotations.Nullable;

public interface DissolutionPlayer {

    @Nullable
    RemnantCapability getRemnantCapability();

    void setRemnantCapability(RemnantCapability dissolution_remnantCapability);
}
