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

import baritone.api.Settings;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import com.google.common.collect.ImmutableMap;
import ladysnake.requiem.common.entity.PlayerShellEntity;
import ladysnake.requiem.common.entity.ai.brain.AutomatoneWalkTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

public class PlayerWanderAroundTask extends Task<PlayerShellEntity> {
    public PlayerWanderAroundTask() {
        super(ImmutableMap.of(
            MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT
        ), Integer.MAX_VALUE);
    }

    @Override
    protected void run(ServerWorld world, PlayerShellEntity executor, long time) {
        WalkTarget target = executor.getBrain().getOptionalMemory(MemoryModuleType.WALK_TARGET).orElseThrow(IllegalStateException::new);
        float speed = target.getSpeed();
        Settings settings = executor.getBaritone().settings();
        if (speed <= 0) {
            throw new IllegalStateException("Some task asked to walk towards something with a negative speed ?!");
        }
        settings.allowSprint.set(speed > 0.5);
        settings.allowParkour.set(speed >= 1.);
        double terrainModificationFactor = 1. / MathHelper.clamp(speed * speed, 0.1, 1.);
        settings.blockBreakAdditionalPenalty.set(settings.blockBreakAdditionalPenalty.defaultValue() * terrainModificationFactor);
        settings.blockPlacementPenalty.set(settings.blockPlacementPenalty.defaultValue() * terrainModificationFactor);
        executor.getPathfindingProcess().setGoal(this.getGoal(target));
    }

    private Goal getGoal(WalkTarget target) {
        if (target instanceof AutomatoneWalkTarget) {
            return ((AutomatoneWalkTarget) target).getGoal();
        }
        return new GoalNear(target.getLookTarget().getBlockPos(), target.getCompletionRange());
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, PlayerShellEntity entity, long time) {
        return entity.getPathfindingProcess().isActive();
    }
}
