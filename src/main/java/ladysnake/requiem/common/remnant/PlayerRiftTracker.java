/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.common.remnant;

import com.google.common.base.Preconditions;
import ladysnake.requiem.api.v1.block.ObeliskDescriptor;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.api.v1.remnant.RiftTracker;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PlayerRiftTracker implements RiftTracker {
    private final PlayerEntity player;
    private final Set<UUID> riftRecordUuids;

    public PlayerRiftTracker(PlayerEntity player) {
        this.player = player;
        this.riftRecordUuids = new HashSet<>();
    }

    @Override
    public void addRift(GlobalRecord riftRecord) {
        Preconditions.checkArgument(riftRecord.has(RequiemRecordTypes.RIFT_OBELISK));
        Preconditions.checkArgument(riftRecord.has(RequiemRecordTypes.OBELISK_REF));

        if (this.riftRecordUuids.add(riftRecord.getUuid()) && this.player instanceof ServerPlayerEntity sp) {
            Optional<Text> obeliskName = riftRecord.get(RequiemRecordTypes.OBELISK_REF).flatMap(ObeliskDescriptor::name);
            RequiemNetworking.sendRiftWitnessedMessage(sp, obeliskName.orElse(Text.empty()));
        }
    }

    @Override
    public Set<ObeliskDescriptor> fetchKnownObelisks() {
        GlobalRecordKeeper recordKeeper = GlobalRecordKeeper.get(this.player.getWorld());
        Set<ObeliskDescriptor> ret = new HashSet<>();

        for (Iterator<UUID> iterator = this.riftRecordUuids.iterator(); iterator.hasNext(); ) {
            UUID uuid = iterator.next();
            Optional<ObeliskDescriptor> descriptor = recordKeeper.getRecord(uuid).filter(r -> r.get(RequiemRecordTypes.RIFT_OBELISK).isPresent()).flatMap(r -> r.get(RequiemRecordTypes.OBELISK_REF));
            if (descriptor.isEmpty()) {
                iterator.remove();
            } else {
                ret.add(descriptor.get());
            }
        }

        return ret;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.riftRecordUuids.clear();
        for (NbtElement uuid : tag.getList("rift_records", NbtElement.INT_ARRAY_TYPE)) {
            this.riftRecordUuids.add(NbtHelper.toUuid(uuid));
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList list = new NbtList();
        for (UUID uuid : this.riftRecordUuids) {
            list.add(NbtHelper.fromUuid(uuid));
        }
        tag.put("rift_records", list);
    }
}
