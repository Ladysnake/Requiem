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
package ladysnake.pandemonium.common.entity.ai.brain.task.generic;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Holder;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Optional;

public class GenerifiedVillagerWalkTowardsTask extends Task<PathAwareEntity> {

    public static void releaseAllTickets(LivingEntity mob) {
        releaseTicketFor(mob, MemoryModuleType.HOME);
        releaseTicketFor(mob, MemoryModuleType.JOB_SITE);
        releaseTicketFor(mob, MemoryModuleType.POTENTIAL_JOB_SITE);
        releaseTicketFor(mob, MemoryModuleType.MEETING_POINT);
    }

    // Copy of VillagerEntity#releaseTicketFor
    public static void releaseTicketFor(LivingEntity mob, MemoryModuleType<GlobalPos> memoryType) {
        if (mob.getBrain().hasMemoryModule(memoryType)) {
            GlobalPos pos = mob.getBrain().getOptionalMemory(memoryType).orElseThrow();
            ServerWorld serverWorld = ((ServerWorld) mob.world).getServer().getWorld(pos.getDimension());

            if (serverWorld != null) {
                PointOfInterestStorage pointOfInterestStorage = serverWorld.getPointOfInterestStorage();
                Optional<Holder<PointOfInterestType>> optional = pointOfInterestStorage.getType(pos.getPos());
                // Diff: there should be a test using VillagerEntity#POINTS_OF_INTEREST here but bleeeeh it requires villagers
                if (optional.isPresent()) {
                    pointOfInterestStorage.releaseTicket(pos.getPos());
                    DebugInfoSender.sendPointOfInterest(serverWorld, pos.getPos());
                }
            }
        }
    }

    private final MemoryModuleType<GlobalPos> destination;
	private final float speed;
	private final int completionRange;
	private final int maxRange;
	private final int maxRunTime;

    public GenerifiedVillagerWalkTowardsTask(MemoryModuleType<GlobalPos> memoryModuleType, float speed, int completionRange, int maxRange, int maxRunTime) {
		super(
			ImmutableMap.of(
				MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
				MemoryModuleState.REGISTERED,
				MemoryModuleType.WALK_TARGET,
				MemoryModuleState.VALUE_ABSENT,
				memoryModuleType,
				MemoryModuleState.VALUE_PRESENT
			)
		);
		this.destination = memoryModuleType;
		this.speed = speed;
		this.completionRange = completionRange;
		this.maxRange = maxRange;
		this.maxRunTime = maxRunTime;
	}

	private void giveUp(PathAwareEntity villager, long time) {
		Brain<?> brain = villager.getBrain();
		releaseTicketFor(villager, this.destination);
		brain.forget(this.destination);
		brain.remember(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time);
	}

	@Override
    protected void run(ServerWorld serverWorld, PathAwareEntity villagerEntity, long l) {
		Brain<?> brain = villagerEntity.getBrain();
		brain.getOptionalMemory(this.destination)
			.ifPresent(
				globalPos2 -> {
					if (this.dimensionMismatches(serverWorld, globalPos2) || this.shouldGiveUp(serverWorld, villagerEntity)) {
						this.giveUp(villagerEntity, l);
					} else if (this.exceedsMaxRange(villagerEntity, globalPos2)) {
						Vec3d vec3d = null;
						int i = 0;

						for(int j = 1000;
							i < 1000 && (vec3d == null || this.exceedsMaxRange(villagerEntity, GlobalPos.create(serverWorld.getRegistryKey(), new BlockPos(vec3d))));
							++i
						) {
							vec3d = NoPenaltyTargeting.find(villagerEntity, 15, 7, Vec3d.ofBottomCenter(globalPos2.getPos()), (float) (Math.PI / 2));
						}

						if (i == 1000) {
							this.giveUp(villagerEntity, l);
							return;
						}

						brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3d, this.speed, this.completionRange));
					} else if (!this.reachedDestination(serverWorld, villagerEntity, globalPos2)) {
						brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(globalPos2.getPos(), this.speed, this.completionRange));
					}

				}
			);
	}

	private boolean shouldGiveUp(ServerWorld world, PathAwareEntity villager) {
		Optional<Long> optional = villager.getBrain().getOptionalMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		if (optional.isPresent()) {
			return world.getTime() - optional.get() > (long)this.maxRunTime;
		} else {
			return false;
		}
	}

	private boolean exceedsMaxRange(PathAwareEntity villager, GlobalPos pos) {
		return pos.getPos().getManhattanDistance(villager.getBlockPos()) > this.maxRange;
	}

	private boolean dimensionMismatches(ServerWorld world, GlobalPos pos) {
		return pos.getDimension() != world.getRegistryKey();
	}

	private boolean reachedDestination(ServerWorld world, PathAwareEntity villager, GlobalPos pos) {
		return pos.getDimension() == world.getRegistryKey() && pos.getPos().getManhattanDistance(villager.getBlockPos()) <= this.completionRange;
	}
}
