package ladysnake.requiem.api.v1.remnant;

import java.util.UUID;

@FunctionalInterface
public interface FractureAnchorFactory {
    FractureAnchor create(FractureAnchorManager manager, UUID persistentId, int networkId);
}
