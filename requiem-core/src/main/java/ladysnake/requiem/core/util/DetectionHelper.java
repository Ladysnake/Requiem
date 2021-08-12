/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * Simple mob detection helper thingy
 *
 * @author SciRave
 */
public class DetectionHelper {

    //Controls what is defined as a valid enemy for the system to anger.
    public static boolean isValidEnemy(Entity mob) {
        return mob instanceof HostileEntity && !(mob instanceof Angerable);
    }

    //Incites an individual mob to attack the host of a demon.
    public static void inciteMob(MobEntity host, HostileEntity mob) {
        mob.setTarget(host);
        mob.getBrain().remember(MemoryModuleType.ANGRY_AT, host.getUuid(), 600L);
    }

    //Incites an individual mob and their buddies in a range. Currently it is capped at 50 because no vanilla mob usually exceeds that.
    public static void inciteMobAndAllies(MobEntity host, HostileEntity mob) {
        inciteMob(host, mob);

        List<HostileEntity> sawExchange = mob.world.getEntitiesByClass(HostileEntity.class, Box.from(mob.getPos()).expand(50, 50, 50), hostileEntity -> isValidEnemy(hostileEntity) && hostileEntity.isInWalkTargetRange(host.getBlockPos()) && hostileEntity.canSee(host));

        sawExchange.forEach(hostileEntity -> inciteMob(host, hostileEntity));

    }

}
