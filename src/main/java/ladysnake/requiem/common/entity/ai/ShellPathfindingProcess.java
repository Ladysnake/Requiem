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
package ladysnake.requiem.common.entity.ai;

import baritone.api.IBaritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import org.jetbrains.annotations.Nullable;

public class ShellPathfindingProcess implements IBaritoneProcess {
    private boolean executing;
    private @Nullable Goal goal;
    private final IBaritone baritone;

    public ShellPathfindingProcess(IBaritone baritone) {
        this.baritone = baritone;
    }

    public void setGoal(@Nullable Goal goal) {
        this.goal = goal;
        this.executing = false;
    }

    @Override
    public boolean isActive() {
        return this.goal != null;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (!this.executing) {
            PathingCommand ret = new PathingCommand(this.goal, PathingCommandType.FORCE_REVALIDATE_GOAL_AND_PATH);
            this.executing = true;
            return ret;
        }

        if (!calcFailed) {
            if (this.goal != null && (!this.goal.isInGoal(this.baritone.getPlayerContext().feetPos()) || !this.goal.isInGoal(this.baritone.getPathingBehavior().pathStart()))) {
                return new PathingCommand(this.goal, PathingCommandType.SET_GOAL_AND_PATH);
            }
        }

        Brain<?> brain = this.baritone.getPlayerContext().entity().getBrain();
        brain.forget(MemoryModuleType.WALK_TARGET);
        brain.remember(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, this.baritone.getPlayerContext().world().getTime());
        this.onLostControl();
        return new PathingCommand(null, PathingCommandType.CANCEL_AND_SET_GOAL);
    }

    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public void onLostControl() {
        this.goal = null;
    }

    @Override
    public String displayName0() {
        return "Pathfinding Goal " + this.goal;
    }
}
