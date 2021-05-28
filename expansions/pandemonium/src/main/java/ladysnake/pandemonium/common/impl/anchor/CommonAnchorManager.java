/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.pandemonium.common.impl.anchor;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.api.anchor.FractureAnchorFactory;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommonAnchorManager implements FractureAnchorManager, AutoSyncedComponent {
    public static final byte ANCHOR_SYNC = 0;
    public static final byte ANCHOR_REMOVE = 1;

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
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        writeToPacket(buf, this.getAnchors(), ANCHOR_SYNC);
    }

    public static void writeToPacket(PacketByteBuf buf, Collection<FractureAnchor> anchors, byte action) {
        buf.writeVarInt(anchors.size());
        for (FractureAnchor anchor : anchors) {
            buf.writeVarInt(anchor.getId());
            buf.writeByte(action);

            if (action == ANCHOR_SYNC) {
                buf.writeDouble(anchor.getX());
                buf.writeDouble(anchor.getY());
                buf.writeDouble(anchor.getZ());
            }
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (!tag.contains("Anchors", NbtType.LIST)) {
            Requiem.LOGGER.error("Invalid save data. Expected list of FractureAnchors, found none. Discarding save data.");
            return;
        }
        NbtList list = tag.getList("Anchors", NbtType.COMPOUND);
        for (NbtElement anchorNbt : list) {
            NbtCompound anchorTag = (NbtCompound) anchorNbt;
            FractureAnchor anchor = this.addAnchor(AnchorFactories.fromTag(anchorTag));
            if (anchorTag.contains("X", NbtType.DOUBLE)) {
                anchor.setPosition(anchorTag.getDouble("X"), anchorTag.getDouble("Y"), anchorTag.getDouble("Z"));
            } else {
                Requiem.LOGGER.error("Invalid save data. Expected position information, found none. Skipping.");
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList list = new NbtList();
        for (FractureAnchor anchor : this.getAnchors()) {
            list.add(anchor.toTag(new NbtCompound()));
        }
        tag.put("Anchors", list);
    }
}
