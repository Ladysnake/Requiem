package ladysnake.requiem.api.v1;

import ladysnake.requiem.api.v1.remnant.FractureAnchorManager;

/**
 * Implemented by {@link net.minecraft.world.World}, allows access
 * to requiem-specific world functionality.
 */
public interface RequiemWorld {
    FractureAnchorManager getAnchorManager();
}
