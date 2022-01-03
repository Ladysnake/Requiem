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
package ladysnake.requiem.common.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ladysnake.requiem.common.entity.PlayerShellEntity;
import ladysnake.requiem.common.entity.ai.brain.tasks.LivingApproachTargetTask;
import ladysnake.requiem.common.entity.ai.brain.tasks.LivingForgetAttackTargetTask;
import ladysnake.requiem.common.entity.ai.brain.tasks.LivingUpdateAttackTargetTask;
import ladysnake.requiem.common.entity.ai.brain.tasks.PlayerGoHomeTask;
import ladysnake.requiem.common.entity.ai.brain.tasks.PlayerLookAroundTask;
import ladysnake.requiem.common.entity.ai.brain.tasks.PlayerMeleeTask;
import ladysnake.requiem.common.entity.ai.brain.tasks.PlayerWanderAroundTask;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FollowMobTask;
import net.minecraft.entity.ai.brain.task.TimeLimitedTask;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.Optional;

public final class PlayerShellBrain {

    public static Brain<PlayerShellEntity> create(Brain<PlayerShellEntity> brain) {
        addCoreTasks(brain);
        addIdleTasks(brain);
        addFightTasks(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreTasks(Brain<PlayerShellEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new PlayerLookAroundTask(45, 90), new PlayerWanderAroundTask()));
    }

    private static void addIdleTasks(Brain<PlayerShellEntity> brain) {
        brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(
            new PlayerGoHomeTask(MemoryModuleType.HOME, 1f, 10, 5000),  // TODO fix the attempt thing
            new LivingUpdateAttackTargetTask<>(PlayerShellBrain::canFight, PlayerShellBrain::getPriorityAttackTarget),
            new TimeLimitedTask<>(new FollowMobTask(8.0F), UniformIntProvider.create(30, 60))
        ));
    }

    private static Optional<? extends LivingEntity> getPriorityAttackTarget(PlayerShellEntity shell) {
        return shell.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_HOSTILE);
    }

    private static boolean canFight(PlayerShellEntity shell) {
        return shell.getHealth() > 4F;
    }

    private static void addFightTasks(Brain<PlayerShellEntity> brain) {
        brain.setTaskList(Activity.FIGHT, 10,
            ImmutableList.of(
                new LivingApproachTargetTask(1.0F),
                new PlayerMeleeTask(),
                // if an entity is no longer interested in our death, we can leave it be
                new LivingForgetAttackTargetTask<>(e -> e instanceof HostileEntity && ((HostileEntity) e).getTarget() == null)
            ),
            MemoryModuleType.ATTACK_TARGET
        );
    }

    public static void refreshActivities(PlayerShellEntity shell) {
        Brain<PlayerShellEntity> brain = shell.getBrain();
        Activity before = brain.getFirstPossibleNonCoreActivity().orElse(null);
        brain.resetPossibleActivities(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
        Activity after = brain.getFirstPossibleNonCoreActivity().orElse(null);
        if (before != after) {
            getActivityTransitionSound(shell).ifPresent(shell::playSound);
        }
    }

    public static Optional<SoundEvent> getActivityTransitionSound(PlayerShellEntity shell) {
        return shell.getBrain().getFirstPossibleNonCoreActivity().map(PlayerShellBrain::getActivityStartSound);
    }

    private static SoundEvent getActivityStartSound(Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.ENTITY_PIGLIN_ANGRY;
        } else {
            return SoundEvents.ENTITY_PIGLIN_AMBIENT;
        }
    }
}
