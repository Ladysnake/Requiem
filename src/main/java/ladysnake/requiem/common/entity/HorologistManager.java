/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public final class HorologistManager implements Component {

    private boolean busy = false;

    public Optional<HorologistEntity> trySpawnHorologistAround(ServerWorld world, BlockPos pos) {

        if (isHorologistBusy()) {
            return Optional.empty();
        }

        int xCenter = pos.getX();
        int yCenter = pos.getY();
        int zCenter = pos.getZ();

        int xMin = xCenter - 2;
        int zMin = zCenter - 2;
        int xMax = xMin + 2;
        int zMax = zMin + 2;

        return Optional.empty();
    }

    public Optional<HorologistEntity> trySpawnHorologistAt(ServerWorld world, BlockPos.Mutable pos) {
        return Optional.empty();
    }

    public boolean isHorologistBusy() {
        return this.busy;
    }

    public void freeHorologist() {
        this.busy = false;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.busy = tag.getBoolean("busy");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putBoolean("busy", this.busy);
        return tag;
    }
}
