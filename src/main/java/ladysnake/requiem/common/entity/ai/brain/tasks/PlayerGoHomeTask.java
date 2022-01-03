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

import com.google.common.collect.ImmutableMap;
import ladysnake.requiem.common.entity.ai.brain.PandemoniumMemoryModules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.GlobalPos;

import java.util.Optional;

public class PlayerGoHomeTask extends Task<LivingEntity> {
    private final MemoryModuleType<GlobalPos> destination;
    private final float speed;
    private final int completionRange;
    private final int maxAttempts;

    public PlayerGoHomeTask(MemoryModuleType<GlobalPos> destination, float speed, int completionRange, int maxAttempts) {
        super(ImmutableMap.of(
            PandemoniumMemoryModules.GO_HOME_ATTEMPTS, MemoryModuleState.REGISTERED,
            MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT,
            destination, MemoryModuleState.VALUE_PRESENT
        ));
        this.destination = destination;
        this.speed = speed;
        this.completionRange = completionRange;
        this.maxAttempts = maxAttempts;
    }

    private void giveUp(LivingEntity executor, long time) {
        Brain<?> brain = executor.getBrain();
        brain.remember(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, time);
    }

    @Override
    protected boolean shouldRun(ServerWorld world, LivingEntity executor) {
        return !this.shouldGiveUp(executor);
    }

    @Override
    protected void run(ServerWorld serverWorld, LivingEntity executor, long l) {
        Brain<?> brain = executor.getBrain();
        brain.getOptionalMemory(this.destination).ifPresent((globalPos) -> {
            if (!this.isInOtherDimension(serverWorld, globalPos) && !this.shouldGiveUp(executor)) {
                if (this.reachedDestination(serverWorld, executor, globalPos)) {
                    brain.forget(PandemoniumMemoryModules.GO_HOME_ATTEMPTS);
                } else {
                    brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(globalPos.getPos(), this.speed, this.completionRange));
                    brain.remember(PandemoniumMemoryModules.GO_HOME_ATTEMPTS, brain.getOptionalMemory(PandemoniumMemoryModules.GO_HOME_ATTEMPTS).orElse(0) + 1);
                }
            } else {
                this.giveUp(executor, l);
            }
        });
    }

    private boolean shouldGiveUp(LivingEntity executor) {
        Optional<Integer> optional = executor.getBrain().getOptionalMemory(PandemoniumMemoryModules.GO_HOME_ATTEMPTS);
        return optional.filter(attempts -> attempts > this.maxAttempts).isPresent();
    }

    private boolean isInOtherDimension(ServerWorld currentWorld, GlobalPos target) {
        return target.getDimension() != currentWorld.getRegistryKey();
    }

    private boolean reachedDestination(ServerWorld world, LivingEntity executor, GlobalPos pos) {
        return pos.getDimension() == world.getRegistryKey() && pos.getPos().getManhattanDistance(executor.getBlockPos()) <= this.completionRange;
    }
}
