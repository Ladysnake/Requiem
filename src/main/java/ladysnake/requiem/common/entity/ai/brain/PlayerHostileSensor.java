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

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class PlayerHostileSensor extends Sensor<PlayerEntity> {
    public PlayerHostileSensor() {
        super(10);
    }

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_HOSTILE);
    }

    @Override
    protected void sense(ServerWorld world, PlayerEntity entity) {
        entity.getBrain().remember(MemoryModuleType.NEAREST_HOSTILE, this.getNearestHostile(entity));
    }

    private Optional<LivingEntity> getNearestHostile(PlayerEntity subject) {
        // Note: method_38980 prunes mobs further than 16 blocks
        // Probably not a good thing but oh well
        return this.getVisibleMobs(subject).flatMap(mobs -> mobs.stream(entity -> {
            if (!this.isHostile(subject, entity)) return false;
            return this.isCloseEnoughForDanger(subject, entity);
        }).min(Comparator.comparing(subject::squaredDistanceTo)));
    }

    private boolean isCloseEnoughForDanger(PlayerEntity subject, LivingEntity entity) {
        if (entity instanceof MobEntity mobEntity) {
            Item item = mobEntity.getMainHandStack().getItem();
            if (item instanceof RangedWeaponItem && mobEntity.canUseRangedWeapon((RangedWeaponItem) item)) {
                return mobEntity.isInRange(subject, ((RangedWeaponItem) item).getRange());
            } else {
                return mobEntity.isInRange(subject, 6);
            }
        }
        return entity instanceof PlayerEntity;  // players are *always* dangerous if hostile
    }

    private boolean isHostile(PlayerEntity subject, LivingEntity livingEntity) {
        return livingEntity instanceof MobEntity && ((MobEntity) livingEntity).getTarget() == subject;
    }

    private Optional<LivingTargetCache> getVisibleMobs(LivingEntity entity) {
        return entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS);
    }
}
