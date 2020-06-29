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
package ladysnake.pandemonium.common.impl.anchor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.api.anchor.FractureAnchorFactory;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.network.RequiemNetworking;
import nerdhub.cardinal.components.api.util.sync.WorldSyncedComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ladysnake.pandemonium.common.network.PandemoniumNetworking.createAnchorUpdateMessage;

public class CommonAnchorManager implements FractureAnchorManager, WorldSyncedComponent {
    private final Map<UUID, FractureAnchor> anchorsByUuid = new HashMap<>();
    private final Int2ObjectMap<FractureAnchor> anchorsById = new Int2ObjectOpenHashMap<>();
    private final World world;
    private int nextId;

    public CommonAnchorManager(World world) {
        this.world = world;
    }

    @Override
    public FractureAnchor addAnchor(FractureAnchorFactory anchorFactory) {
        return addAnchor(anchorFactory, UUID.randomUUID(), getNextId());
    }

    protected FractureAnchor addAnchor(FractureAnchorFactory anchorFactory, UUID uuid, int id) {
        FractureAnchor anchor = anchorFactory.create(this, uuid, id);
        anchorsByUuid.put(anchor.getUuid(), anchor);
        anchorsById.put(anchor.getId(), anchor);

        return anchor;
    }

    private int getNextId() {
        // Guarantee that the next id is unused
        while (anchorsById.containsKey(nextId)) {
            nextId++;
        }
        return nextId;
    }

    @Override
    public Collection<FractureAnchor> getAnchors() {
        return this.anchorsById.values();
    }

    @Override
    public void updateAnchors(long time) {
        this.anchorsById.values().removeIf(anchor -> {
            anchor.update();
            if (anchor.isInvalid()) {
                this.anchorsByUuid.remove(anchor.getUuid());
                return true;
            }
            return false;
        });
    }

    @Nullable
    @Override
    public FractureAnchor getAnchor(int anchorId) {
        return checkValidity(anchorsById.get(anchorId));
    }

    @Nullable
    @Override
    public FractureAnchor getAnchor(UUID anchorUuid) {
        return checkValidity(anchorsByUuid.get(anchorUuid));
    }

    @Nullable
    private static FractureAnchor checkValidity(@Nullable FractureAnchor anchor) {
        return anchor == null || anchor.isInvalid() ? null : anchor;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public void syncWith(ServerPlayerEntity player) {
        for (FractureAnchor anchor : FractureAnchorManager.get(world).getAnchors()) {
            RequiemNetworking.sendTo(player, createAnchorUpdateMessage(anchor));
        }
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (!tag.contains("Anchors", NbtType.LIST)) {
            Requiem.LOGGER.error("Invalid save data. Expected list of FractureAnchors, found none. Discarding save data.");
            return;
        }
        ListTag list = tag.getList("Anchors", NbtType.COMPOUND);
        for (Tag anchorNbt : list) {
            CompoundTag anchorTag = (CompoundTag) anchorNbt;
            FractureAnchor anchor = this.addAnchor(AnchorFactories.fromTag(anchorTag));
            if (anchorTag.contains("X", NbtType.DOUBLE)) {
                anchor.setPosition(anchorTag.getDouble("X"), anchorTag.getDouble("Y"), anchorTag.getDouble("Z"));
            } else {
                Requiem.LOGGER.error("Invalid save data. Expected position information, found none. Skipping.");
            }
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag list = new ListTag();
        for (FractureAnchor anchor : this.getAnchors()) {
            list.add(anchor.toTag(new CompoundTag()));
        }
        tag.put("Anchors", list);
        return tag;
    }
}
