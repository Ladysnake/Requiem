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
package ladysnake.requiem.compat;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import ladysnake.requiem.Requiem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ComponentDataHolder<C extends Component> implements Component {

    protected final ComponentKey<C> dataKey;
    protected final ComponentKey<?> selfKey;
    protected @Nullable NbtCompound data;

    public ComponentDataHolder(ComponentKey<C> dataKey, ComponentKey<?> selfKey) {
        this.dataKey = dataKey;
        this.selfKey = selfKey;
    }

    public void copyDataBetween(PlayerEntity from, PlayerEntity to) {
        writePlayerDataToNbt(from).ifPresent(nbt -> readPlayerDataFromNbt(to, nbt));
    }

    public void storeDataFrom(PlayerEntity player, boolean override) {
        if (!player.world.isClient && (this.data == null || override)) {
            writePlayerDataToNbt(player).ifPresent(d -> this.data = d);
        }
    }

    private Optional<NbtCompound> writePlayerDataToNbt(PlayerEntity player) {
        try {
            NbtCompound originData = new NbtCompound();
            this.dataKey.get(player).writeToNbt(originData);
            return Optional.of(originData);
        } catch (RuntimeException e) {
            Requiem.LOGGER.error("[Requiem] Failed to serialize data from " + this.dataKey.getId(), e);
            return Optional.empty();
        }
    }

    public void restoreDataToPlayer(PlayerEntity player, boolean clear) {
        if (!player.world.isClient && this.data != null) {
            readPlayerDataFromNbt(player, this.data);
            if (clear) this.data = null;
        }
    }

    private void readPlayerDataFromNbt(PlayerEntity player, NbtCompound data) {
        C component = this.dataKey.get(player);
        NbtCompound backup = Util.make(new NbtCompound(), component::writeToNbt);
        try {
            component.readFromNbt(data);
        } catch (RuntimeException e) {
            Requiem.LOGGER.error("[Requiem] Failed to deserialize data from " + this.dataKey.getId(), e);
            component.readFromNbt(backup);
        }
        this.dataKey.sync(player);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("originData")) {
            this.data = tag.getCompound("originData");
        } else if (tag.contains("componentData")) {
            this.data = tag.getCompound("componentData");
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (this.data != null) {
            tag.put("componentData", this.data);
        }
    }
}
