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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Literally {@link net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask}, but with a higher type bound
 */
public class LivingUpdateAttackTargetTask<E extends LivingEntity> extends Task<E> {
    private final Predicate<E> startCondition;
    private final Function<E, Optional<? extends LivingEntity>> targetGetter;

    public LivingUpdateAttackTargetTask(Predicate<E> startCondition, Function<E, Optional<? extends LivingEntity>> targetGetter) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleState.REGISTERED));
        this.startCondition = startCondition;
        this.targetGetter = targetGetter;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, E executor) {
        if (!this.startCondition.test(executor)) {
            return false;
        } else {
            Optional<? extends LivingEntity> optional = this.targetGetter.apply(executor);
            return optional.isPresent() && optional.get().isAlive();
        }
    }

    @Override
    protected void run(ServerWorld serverWorld, E executor, long l) {
        this.targetGetter.apply(executor).ifPresent((livingEntity) -> this.updateAttackTarget(executor, livingEntity));
    }

    private void updateAttackTarget(E executor, LivingEntity target) {
        executor.getBrain().remember(MemoryModuleType.ATTACK_TARGET, target);
        executor.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }
}
