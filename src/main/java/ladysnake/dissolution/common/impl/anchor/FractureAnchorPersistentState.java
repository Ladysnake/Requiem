package ladysnake.dissolution.common.impl.anchor;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.remnant.FractureAnchor;
import ladysnake.dissolution.api.v1.remnant.FractureAnchorManager;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.PersistentState;

public class FractureAnchorPersistentState extends PersistentState {
    private final FractureAnchorManager manager;

    public FractureAnchorPersistentState(String id, FractureAnchorManager manager) {
        super(id);
        this.manager = manager;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (!tag.containsKey("Anchors", NbtType.LIST)) {
            Dissolution.LOGGER.error("Invalid save data. Expected list of FractureAnchors, found none. Discarding save data.");
            return;
        }
        ListTag list = tag.getList("Anchors", NbtType.COMPOUND);
        for (Tag anchorNbt : list) {
            CompoundTag anchorTag = (CompoundTag) anchorNbt;
            FractureAnchor anchor = this.manager.addAnchor(AnchorFactories.fromTag(anchorTag));
            if (anchorTag.containsKey("X", NbtType.DOUBLE)) {
                anchor.setPosition(anchorTag.getDouble("X"), anchorTag.getDouble("Y"), anchorTag.getDouble("Z"));
            } else {
                Dissolution.LOGGER.error("Invalid save data. Expected position information, found none. Skipping.");
            }
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag list = new ListTag();
        for (FractureAnchor anchor : this.manager.getAnchors()) {
            list.add(anchor.toTag(new CompoundTag()));
        }
        tag.put("Anchors", list);
        return tag;
    }
}
