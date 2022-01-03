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
package ladysnake.requiem.api.v1.remnant;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@API(status = API.Status.EXPERIMENTAL)
public interface VagrantInteractionRegistry {
    /**
     * Registers an interaction between a {@linkplain RemnantComponent#isVagrant() vagrant} player and a set of mobs
     *
     * @param targetType   the upper bound for the type of mobs targeted by this interaction
     * @param precondition a {@link BiPredicate} checking whether the interaction can occur given a target and an executor
     * @param action       the action to perform when this interaction runs
     * @param <E>          the type of entities that can be targeted
     */
    <E extends LivingEntity> void registerPossessionInteraction(Class<E> targetType, BiPredicate<E, PlayerEntity> precondition, BiConsumer<E, PlayerEntity> action);

    /**
     * Registers an interaction between a {@linkplain RemnantComponent#isVagrant() vagrant} player and a set of mobs
     *
     * @param targetType   the upper bound for the type of mobs targeted by this interaction
     * @param precondition a {@link BiPredicate} checking whether the interaction can occur given a target and an executor
     * @param action       the action to perform when this interaction runs
     * @param icon         an {@link Identifier} for the texture to display when this interaction can occur
     * @param <E>          the type of entities that can be targeted
     */
    @API(status = API.Status.EXPERIMENTAL)  // the Identifier is especially experimental (may switch to a more general interaction ID)
    <E extends LivingEntity> void registerPossessionInteraction(Class<E> targetType, BiPredicate<E, PlayerEntity> precondition, BiConsumer<E, PlayerEntity> action, Identifier icon);
}
