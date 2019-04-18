/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.common.impl.anchor;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.FractureAnchor;
import ladysnake.requiem.api.v1.remnant.FractureAnchorManager;
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
            Requiem.LOGGER.error("Invalid save data. Expected list of FractureAnchors, found none. Discarding save data.");
            return;
        }
        ListTag list = tag.getList("Anchors", NbtType.COMPOUND);
        for (Tag anchorNbt : list) {
            CompoundTag anchorTag = (CompoundTag) anchorNbt;
            FractureAnchor anchor = this.manager.addAnchor(AnchorFactories.fromTag(anchorTag));
            if (anchorTag.containsKey("X", NbtType.DOUBLE)) {
                anchor.setPosition(anchorTag.getDouble("X"), anchorTag.getDouble("Y"), anchorTag.getDouble("Z"));
            } else {
                Requiem.LOGGER.error("Invalid save data. Expected position information, found none. Skipping.");
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
