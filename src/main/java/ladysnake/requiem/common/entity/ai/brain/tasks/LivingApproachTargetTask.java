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
package ladysnake.requiem.common.entity.ai.brain.tasks;

import baritone.api.fakeplayer.FakeServerPlayerEntity;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalRunAway;
import com.google.common.collect.ImmutableMap;
import ladysnake.requiem.common.entity.ai.brain.AutomatoneWalkTarget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class LivingApproachTargetTask extends Task<FakeServerPlayerEntity> {
    private final float speed;

    public LivingApproachTargetTask(float speed) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.REGISTERED));
        this.speed = speed;
    }

    @Override
    protected void run(ServerWorld serverWorld, FakeServerPlayerEntity executor, long l) {
        LivingEntity livingEntity = executor.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).orElseThrow(IllegalStateException::new);
        this.rememberWalkTarget(executor, livingEntity);
    }

    private void rememberWalkTarget(FakeServerPlayerEntity executor, LivingEntity target) {
        Brain<?> brain = executor.getBrain();
        brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(target, true));
        WalkTarget walkTarget = new AutomatoneWalkTarget(
            new EntityLookTarget(target, false),
            this.speed,
            new GoalKeepDistanceWithTarget(
                target.getBlockPos(),
                (int) PlayerMeleeTask.getAttackRange(executor),
                2.5,  // keep enough distance with target to not get hit
                executor.getBlockPos(),
                executor.age - executor.getLastAttackTime() == 1 && executor.getRandom().nextBoolean() ? 1 : 0   // change position after we attack
            )
        );
        brain.remember(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    public static class GoalKeepDistanceWithTarget extends GoalNear {
        private final GoalRunAway avoidTarget;
        private final GoalRunAway avoidSrc;

        public GoalKeepDistanceWithTarget(BlockPos pos, int range, double avoidRange, BlockPos src, double avoidSrcRange) {
            super(pos, range);
            this.avoidTarget = new GoalRunAway(avoidRange, pos);
            this.avoidSrc = new GoalRunAway(avoidSrcRange, src);
        }

        @Override
        public boolean isInGoal(int x, int y, int z) {
            return super.isInGoal(x, y, z) && this.avoidTarget.isInGoal(x, y, z) && this.avoidSrc.isInGoal(x, y, z);
        }

        @Override
        public double heuristic(int x, int y, int z) {
            return Math.max(
                Math.max(super.heuristic(x, y, z), this.avoidTarget.heuristic(x, y, z)),
                this.avoidSrc.heuristic(x, y, z)
            );
        }

        @Override
        public double heuristic() {
            return super.heuristic();
        }

        @Override
        public String toString() {
            return String.format(
                "GoalKeepDistanceWithTarget{x=%s, y=%s, z=%s, rangeSq=%d, secondaries=[%s, %s]}",
                x,
                y,
                z,
                rangeSq,
                avoidTarget,
                avoidSrc
            );
        }
    }
}
