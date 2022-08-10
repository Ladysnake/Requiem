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
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestStorage;

public class GenerifiedGoToPointOfInterestTask extends Task<PathAwareEntity> {
	private final float speed;
	private final int completionRange;

	public GenerifiedGoToPointOfInterestTask(float speed, int completionRange) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
		this.speed = speed;
		this.completionRange = completionRange;
	}

	@Override
    protected boolean shouldRun(ServerWorld serverWorld, PathAwareEntity villagerEntity) {
		return !serverWorld.isNearOccupiedPointOfInterest(villagerEntity.getBlockPos());
	}

	@Override
    protected void run(ServerWorld serverWorld, PathAwareEntity villagerEntity, long l) {
		PointOfInterestStorage pointOfInterestStorage = serverWorld.getPointOfInterestStorage();
		int i = pointOfInterestStorage.getDistanceFromNearestOccupied(ChunkSectionPos.from(villagerEntity.getBlockPos()));
		Vec3d vec3d = null;

		for(int j = 0; j < 5; ++j) {
			Vec3d vec3d2 = FuzzyTargeting.find(
				villagerEntity, 15, 7, pos -> (double)(-pointOfInterestStorage.getDistanceFromNearestOccupied(ChunkSectionPos.from(pos)))
			);
			if (vec3d2 != null) {
				int k = pointOfInterestStorage.getDistanceFromNearestOccupied(ChunkSectionPos.from(new BlockPos(vec3d2)));
				if (k < i) {
					vec3d = vec3d2;
					break;
				}

				if (k == i) {
					vec3d = vec3d2;
				}
			}
		}

		if (vec3d != null) {
			villagerEntity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3d, this.speed, this.completionRange));
		}

	}
}
