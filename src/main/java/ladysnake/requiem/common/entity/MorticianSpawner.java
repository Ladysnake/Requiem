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

import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.block.RiftRunestoneBlock;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;

import java.util.stream.Stream;

public class MorticianSpawner implements ServerTickEvents.EndTick {
    private static final int SPAWN_COOLDOWN = 1200;
    private int ticksUntilNextSpawn;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(new MorticianSpawner());
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        if (server.shouldSpawnAnimals() && server.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
            --this.ticksUntilNextSpawn;
            if (this.ticksUntilNextSpawn <= 0) {
                this.ticksUntilNextSpawn = SPAWN_COOLDOWN;
                streamSpawnableObelisks(server)
                    .forEach(r -> r.get(RequiemRecordTypes.OBELISK_REF).ifPresent(obelisk -> {
                        ServerWorld world = server.getWorld(obelisk.dimension());
                        if (world == null || !world.isRegionLoaded(obelisk.pos().getX() - 10, obelisk.pos().getZ() - 10, obelisk.pos().getZ() + 10, obelisk.pos().getZ() + 10)) return;
                        RiftRunestoneBlock.findRespawnPosition(RequiemEntities.MORTICIAN, world, obelisk.pos()).ifPresent(spawnPos -> {
                            MorticianEntity mortician = RequiemEntities.MORTICIAN.spawn(world, null, null, null, new BlockPos(spawnPos), SpawnReason.STRUCTURE, false, false);
                            if (mortician != null) {
                                Vec3d towardsObelisk = obelisk.center().subtract(spawnPos).normalize();
                                mortician.setYaw((float) MathHelper.wrapDegrees(MathHelper.atan2(towardsObelisk.z, towardsObelisk.x) * 180.0F / (float)Math.PI - 90.0));
                                mortician.linkWith(r);
                            }
                        });
                    }));
            }
        }
    }

    public static Stream<GlobalRecord> streamSpawnableObelisks(MinecraftServer server) {
        return GlobalRecordKeeper.get(server)
            .getRecords().stream()
            // Find all obelisks that have no mortician
            .filter(r -> r.get(RequiemRecordTypes.RIFT_OBELISK).isPresent())
            .filter(r -> r.get(RequiemRecordTypes.MORTICIAN_REF).isEmpty());
    }
}
