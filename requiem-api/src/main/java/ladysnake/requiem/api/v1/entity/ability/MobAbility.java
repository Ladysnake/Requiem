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
package ladysnake.requiem.api.v1.entity.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;

/**
 * A {@link MobAbility} is a special ability wielded by some entities,
 * that ethereal players possessing those entities can take advantage of.
 * Abilities are usually active, they substitute AI goals for possessed entities.
 *
 * @param <E> The type of mobs that can wield this ability
 * @see net.minecraft.entity.ai.goal.Goal
 */
@SuppressWarnings("unused")
public interface MobAbility<E extends LivingEntity> {
    default void update() {
        // NO-OP
    }

    default float getCooldownProgress() {
        return 1.0F;
    }

    default void writeToPacket(PacketByteBuf buf) {
        // NO-OP
    }

    default void readFromPacket(PacketByteBuf buf) {
        // NO-OP
    }
}
