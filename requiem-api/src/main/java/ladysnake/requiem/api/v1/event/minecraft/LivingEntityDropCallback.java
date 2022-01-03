/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.event.minecraft;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@FunctionalInterface
public interface LivingEntityDropCallback {

    /**
     *
     * @param entity the entity that is dropping its items
     * @param deathCause the damage source causing the death
     * @return {@code true} if the drop process should be cancelled
     */
    boolean onEntityDrop(LivingEntity entity, DamageSource deathCause);

    Event<LivingEntityDropCallback> EVENT = EventFactory.createArrayBacked(LivingEntityDropCallback.class,
            (listeners) -> (entity, deathCause) -> {
                for (LivingEntityDropCallback handler : listeners) {
                    if (handler.onEntityDrop(entity, deathCause)) {
                        return true;
                    }
                }
                return false;
            });
}
