package ladysnake.dissolution.common.impl.anchor;

import ladysnake.dissolution.api.v1.remnant.FractureAnchorFactory;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.UUID;

public class AnchorFactories {
    public static FractureAnchorFactory fromEntityUuid(UUID entityUuid) {
        return (manager, uuid, id) -> new EntityFractureAnchor(entityUuid, manager, uuid, id);
    }

    @Nonnull
    public static FractureAnchorFactory fromTag(CompoundTag anchorTag) {
        if (anchorTag.getString("AnchorType").equals("dissolution:entity")) {
            return entityAnchorFromTag(anchorTag);
        }
        return trackedAnchorFromTag(anchorTag);
    }

    private static FractureAnchorFactory entityAnchorFromTag(CompoundTag serializedAnchor) {
        return (manager, uuid, id) -> new EntityFractureAnchor(manager, serializedAnchor, id);
    }

    private static FractureAnchorFactory trackedAnchorFromTag(CompoundTag serializedAnchor) {
        return (manager, uuid, id) -> new TrackedFractureAnchor(manager, serializedAnchor, id);
    }
}
