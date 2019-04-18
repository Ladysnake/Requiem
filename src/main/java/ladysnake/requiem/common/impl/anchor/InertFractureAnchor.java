package ladysnake.requiem.common.impl.anchor;

import ladysnake.requiem.api.v1.remnant.FractureAnchor;
import ladysnake.requiem.api.v1.remnant.FractureAnchorManager;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class InertFractureAnchor implements FractureAnchor {
    protected final FractureAnchorManager manager;
    private final int id;
    private final UUID uuid;
    protected double z;
    protected double x;
    protected double y;
    private boolean invalid;

    public InertFractureAnchor(FractureAnchorManager manager, UUID uuid, int id) {
        this.manager = manager;
        this.id = id;
        this.uuid = uuid;
    }

    protected InertFractureAnchor(FractureAnchorManager manager, CompoundTag tag, int id) {
        this(manager, tag.getUuid("AnchorUuid"), id);
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void update() {
        // NO-OP
    }

    @Override
    public void invalidate() {
        this.invalid = true;
    }

    @Override
    public boolean isInvalid() {
        return this.invalid;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putUuid("AnchorUuid", this.getUuid());
        tag.putDouble("X", this.x);
        tag.putDouble("Y", this.y);
        tag.putDouble("Z", this.z);
        return tag;
    }
}
