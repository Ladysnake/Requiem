package ladysnake.dissolution.api.v1;

import ladysnake.dissolution.api.v1.remnant.FractureAnchorManager;

/**
 * Implemented by {@link net.minecraft.world.World}, allows access
 * to dissolution-specific world functionality.
 */
public interface DissolutionWorld {
    FractureAnchorManager getAnchorManager();
}
