package ladysnake.requiem.api.v1.remnant;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * A {@link FractureAnchor} represents the origin of an ethereal player
 * who left their body through {@link RemnantState#fracture()}.
 * Anchors are tracked regardless of distance and loaded chunks.
 */
public interface FractureAnchor {
    /**
     * Returns the constant shorter ID of the fracture anchor that uniquely identifies the fracture anchor
     * within its {@link FractureAnchorManager}. This ID may change whenever the fracture anchor is
     * loaded from disk and may be reused.
     *
     * @return the constant short ID of the anchor
     */
    int getId();

    /**
     * Returns the constant longer UUID of the fracture anchor that uniquely identifies the fracture anchor
     * within its {@link FractureAnchorManager}. This ID will not change whenever the fracture anchor is
     * loaded from disk and may not be reused.
     *
     * @return the constant UUID of the anchor
     */
    UUID getUuid();

    double getX();

    double getY();

    double getZ();

    void setPosition(double x, double y, double z);

    void update();

    boolean isInvalid();

    void invalidate();

    CompoundTag toTag(CompoundTag tag);
}
