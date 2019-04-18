package ladysnake.requiem.api.v1.remnant;

import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

/**
 * A {@link FractureAnchorManager} tracks origins of ethereal players
 * having left their body through {@link RemnantState#fracture()}.
 * <p>
 * Positions are saved to avoid having to load chunks and keep track
 * of every origin entity. Instead, such entities update their corresponding
 * tracked entry whenever their state changes.
 * <p>
 * The tracker is kept synchronized between server and clients.
 */
public interface FractureAnchorManager {

    /**
     *
     * @param anchorFactory the factory to use to create the anchor to track
     * @return the created anchor
     */
    FractureAnchor addAnchor(FractureAnchorFactory anchorFactory);

    @Nullable FractureAnchor getAnchor(int anchorId);

    @Nullable FractureAnchor getAnchor(UUID anchorUuid);

    Collection<FractureAnchor> getAnchors();

    void updateAnchors(long time);

    World getWorld();
}
