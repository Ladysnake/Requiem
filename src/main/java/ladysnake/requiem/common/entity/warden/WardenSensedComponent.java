/*
 * Requiem
 * Copyright (C) 2017-2024 Ladysnake
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
package ladysnake.requiem.common.entity.warden;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.core.RequiemCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.warden.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WardenSensedComponent implements TransientComponent, AutoSyncedComponent {
    /*TODO proper implementation of syncing
     */
    public static final ComponentKey<WardenSensedComponent> KEY = ComponentRegistry.getOrCreate(RequiemCore.id("warden_sensed"), WardenSensedComponent.class);

    private Set<UUID> entities;

    public WardenSensedComponent(PlayerEntity livingEntity) {
        this.entities = new HashSet<>();
    }

    public Set<UUID> getVisible() {
        return entities;
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        int size = buf.readInt();
        if (size > 0) {
            Set<UUID> uuids = new HashSet<>();
            for (int i=0; i < size; i++) {
                try {
                    uuids.add(buf.readUuid());
                } catch (Exception error) {
                    Requiem.LOGGER.error("Could not read element {}","warden_target_"+i );
                    Requiem.LOGGER.error("Reason: {}", error.getMessage());
                }
            }
            this.entities = uuids;
        }
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {

        //TODO think about a better data format for this
        //MC only seems to have a ByteArray as closest, no UUIDArray or even just StringArray in NBTCompound
        buf.writeInt(entities.size());
        for (int i = 0; i < entities.size(); i++) {
            buf.writeUuid(entities.stream().toList().get(i));
        }
    }

    public WardenSensedComponent setVisibile(Set<Entity> entities) {
        this.entities.addAll(convertToUUID(entities));
        return this;
    }


    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        if (PossessionComponent.get(player).isPossessionOngoing() && PossessionComponent.get(player).getHost() instanceof WardenEntity) {
            return true;
        } else {
            return false;
        }
    }

    public WardenSensedComponent clear() {
        this.entities = new HashSet<>();
        return this;
    }

    private static Set<UUID> convertToUUID(Set<Entity> entities) {
        Set<UUID> idsFromEntities = new HashSet<>();
        entities.forEach(entity -> {
            if (entity != null) {
                idsFromEntities.add(entity.getUuid());
            }
        });
        return idsFromEntities;
    }

    public boolean isEmpty() {
        return this.entities.isEmpty();
    }

    public boolean isVisibleToWarden(Entity entity) {
        return entities.contains(entity.getUuid());
    }

    public void setVisibility(Entity entity, boolean visibile) {
        if (visibile && !entities.contains(entity.getUuid())) {
            entities.add(entity.getUuid());
        }  else {
            entities.remove(entity.getUuid());
        }
    }
}
