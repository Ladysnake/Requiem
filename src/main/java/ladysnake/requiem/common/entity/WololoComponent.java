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
package ladysnake.requiem.common.entity;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import ladysnake.requiem.client.render.entity.ClientWololoComponent;
import ladysnake.requiem.core.RequiemCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class WololoComponent implements AutoSyncedComponent {
    public static final ComponentKey<WololoComponent> KEY = ComponentRegistry.getOrCreate(RequiemCore.id("wololo"), WololoComponent.class);

    public static boolean canBeConverted(Entity entity) {
        WololoComponent w = KEY.getNullable(entity);
        return w != null && w.canBeConverted();
    }

    public static boolean isConverted(Entity entity) {
        WololoComponent w = KEY.getNullable(entity);
        return w != null && w.isConverted();
    }

    public static WololoComponent create(LivingEntity entity) {
        return entity.world.isClient ? new ClientWololoComponent(entity) : new WololoComponent(entity);
    }

    private final LivingEntity entity;
    private boolean converted = false;

    public WololoComponent(LivingEntity entity) {
        this.entity = entity;
    }

    public boolean isConverted() {
        return converted;
    }

    public boolean canBeConverted() {
        return !isConverted();
    }

    public void wololo() {
        if (this.canBeConverted()) {
            this.converted = true;
            KEY.sync(this.entity);
        }
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeBoolean(this.converted);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.converted = buf.readBoolean();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("converted")) {
            this.converted = tag.getBoolean("converted");
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (this.converted) {
            tag.putBoolean("converted", true);
        }
    }
}
