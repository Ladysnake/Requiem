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
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;

import java.util.Optional;

// Copied from WalkTowardJobSiteTask
public class GenerifiedWalkTowardJobSiteTask<E extends LivingEntity> extends Task<E> {

	private static final int RUN_TIME = 1200;
	final float speed;

	public GenerifiedWalkTowardJobSiteTask(float speed) {
		super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleState.VALUE_PRESENT), RUN_TIME);
		this.speed = speed;
	}

	@Override
    protected boolean shouldRun(ServerWorld serverWorld, LivingEntity livingEntity) {
		return livingEntity.getBrain()
			.getFirstPossibleNonCoreActivity()
			.map(activity -> activity == Activity.IDLE || activity == Activity.WORK || activity == Activity.PLAY)
			.orElse(true);
	}

	@Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, LivingEntity livingEntity, long l) {
		return livingEntity.getBrain().hasMemoryModule(MemoryModuleType.POTENTIAL_JOB_SITE);
	}

	@Override
    protected void keepRunning(ServerWorld serverWorld, LivingEntity livingEntity, long l) {
		LookTargetUtil.walkTowards(
			livingEntity, livingEntity.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().getPos(), this.speed, 1
		);
	}

	@Override
    protected void finishRunning(ServerWorld serverWorld, LivingEntity livingEntity, long l) {
		Optional<GlobalPos> optional = livingEntity.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
		optional.ifPresent(globalPos -> {
			BlockPos blockPos = globalPos.getPos();
			ServerWorld serverWorldx = serverWorld.getServer().getWorld(globalPos.getDimension());
			if (serverWorldx != null) {
				PointOfInterestStorage pointOfInterestStorage = serverWorldx.getPointOfInterestStorage();
				if (pointOfInterestStorage.test(blockPos, holder -> true)) {
					pointOfInterestStorage.releaseTicket(blockPos);
				}

				DebugInfoSender.sendPointOfInterest(serverWorld, blockPos);
			}
		});
		livingEntity.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
	}
}
