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
package ladysnake.requiem.core.util;

import ladysnake.requiem.api.v1.event.requiem.PossessionEvents;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.core.mixin.access.MobEntityAccessor;
import ladysnake.requiem.core.mixin.possession.FollowTargetGoalAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * Simple mob detection helper thingy
 *
 * @author SciRave
 */
public final class DetectionHelper {
    public static final int ALERT_RANGE = 50;

    /**
     * Controls what is defined as a valid enemy for the system to anger.
     *
     * @param mob the mob you to check.
     * @return returns a boolean based on whether or not the mob is a valid enemy.
     */
    public static boolean isValidEnemy(MobEntity mob) {
        return mob instanceof HostileEntity && !(mob instanceof Angerable);
    }

    /**
     * Incites an individual mob to attack the host of a demon.
     *
     * @param host the host you want to attack.
     * @param hostile the mob you want to anger.
     */
    public static void inciteMob(MobEntity host, MobEntity hostile) {
        for (PrioritizedGoal goal : ((MobEntityAccessor) hostile).getTargetSelector().getGoals()) {
            if (goal.getGoal() instanceof FollowTargetGoalAccessor g && g.getTargetClass().isAssignableFrom(ServerPlayerEntity.class)) {
                g.setTargetEntity(host);
                goal.start();
            }
        }
        hostile.getBrain().remember(MemoryModuleType.ANGRY_AT, host.getUuid(), 600L);
    }

    /**
     * Incites an individual mob and their buddies in a range.
     *
     * @param host the host you want to be attacked.
     * @param hostile the mob you want to anger and find allies around.
     */
    public static void inciteMobAndAllies(MobEntity host, MobEntity hostile) {
        inciteMob(host, hostile);

        List<HostileEntity> sawExchange = hostile.world.getEntitiesByClass(HostileEntity.class, Box.from(hostile.getPos()).expand(ALERT_RANGE), witness -> isValidEnemy(witness) && witness.isInWalkTargetRange(host.getBlockPos()) && witness.canSee(host));

        for (HostileEntity witness : sawExchange) {
            inciteMob(host, witness);
        }
    }

    public static void attemptDetection(MobEntity sensed, Entity sensor, PossessionEvents.DetectionAttempt.DetectionReason reason) {
        if (canBeDetected(sensed) && sensor instanceof MobEntity mob && isValidEnemy(mob)) {
            switch (PossessionEvents.DETECTION_ATTEMPT.invoker().shouldDetect(sensed, mob, reason)) {
                case DETECTED -> inciteMob(sensed, mob);
                case CROWD_DETECTED -> inciteMobAndAllies(sensed, mob);
            }
        }
    }

    public static boolean canBeDetected(MobEntity candidate) {
        PlayerEntity spy = ((Possessable) candidate).getPossessor(); // it could be you, it could be me!
        return spy != null && !spy.getAbilities().creativeMode;
    }
}
