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

import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

import static ladysnake.pandemonium.common.network.PandemoniumNetworking.createAnchorDeleteMessage;
import static ladysnake.pandemonium.common.network.PandemoniumNetworking.createAnchorUpdateMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendTo;

public class TrackedFractureAnchor extends InertFractureAnchor {
    public TrackedFractureAnchor(FractureAnchorManager manager, UUID uuid, int id) {
        super(manager, uuid, id);
    }

    protected TrackedFractureAnchor(FractureAnchorManager manager, CompoundTag tag, int id) {
        super(manager, tag, id);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        syncWithWorld(createAnchorUpdateMessage(this));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        syncWithWorld(createAnchorDeleteMessage(this.getId()));
    }

    protected void syncWithWorld(CustomPayloadS2CPacket packet) {
        for (ServerPlayerEntity player : ((ServerWorld) this.manager.getWorld()).getPlayers()) {
            sendTo(player, packet);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag anchorTag) {
        super.toTag(anchorTag);
        anchorTag.putString("AnchorType", "requiem:tracked");
        return anchorTag;
    }
}
