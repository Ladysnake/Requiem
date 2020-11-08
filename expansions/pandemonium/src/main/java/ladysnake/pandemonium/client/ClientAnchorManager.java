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
package ladysnake.pandemonium.client;

import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.common.impl.anchor.CommonAnchorManager;
import ladysnake.pandemonium.common.impl.anchor.InertFractureAnchor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClientAnchorManager extends CommonAnchorManager {
    public ClientAnchorManager(World world) {
        super(world);
    }

    private FractureAnchor getOrCreate(int id) {
        FractureAnchor ret = this.getAnchor(id);
        if (ret == null) {
            ret = addAnchor(InertFractureAnchor::new, UUID.randomUUID(), id);
        }
        return ret;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            int id = buf.readInt();
            byte action = buf.readByte();
            if (action == CommonAnchorManager.ANCHOR_SYNC) {
                updatePosition(buf, this.getOrCreate(id));
            } else if (action == CommonAnchorManager.ANCHOR_REMOVE) {
                removeAnchor(this.getAnchor(id));
            }
        }
    }

    private void removeAnchor(@Nullable FractureAnchor anchor) {
        if (anchor != null) {
            anchor.invalidate();
        }
    }

    private void updatePosition(PacketByteBuf buf, FractureAnchor anchor) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        anchor.setPosition(x, y, z);
    }
}
