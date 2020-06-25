/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.api.v1.entity.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * A {@link DirectAbility} targets a specific entity
 *
 * @param <E> The type of mobs that can wield this ability
 */
@FunctionalInterface
public interface DirectAbility<E extends MobEntity> extends MobAbility<E> {
    /**
     * Triggers the ability on a known entity.
     *
     * @param player the player commanding the ability
     * @param target the targeted entity
     * @return <code>true</code> if the ability has been successfully used
     */
    boolean trigger(PlayerEntity player, Entity target);
}
