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
package ladysnake.requiem.api.v1.possession;

import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

/**
 * A {@link Possessable} entity can be possessed by a player through a {@link PossessionComponent}.
 * When possessed, the entity should stop acting on its own, and act as a delegate body
 * for the possessing player.
 * <p>
 * All methods in this interface have defaults so subclasses can omit implementations.
 * Those default implementations are actually dummies, actual default implementations
 * are provided by {@link net.minecraft.entity.LivingEntity}.
 */
public interface Possessable extends ProtoPossessable {

    /**
     * Returns the {@link PlayerEntity} currently possessing this entity,
     * or {@code null} if there is no player possessing this entity.
     *
     * @return the player currently possessing this entity.
     */
    @Nullable
    @Override
    default PlayerEntity getPossessor() { return null; }

    /**
     * Returns whether this entity has a defined {@link #getPossessor() possessor}.
     *
     * @return {@code true} if this entity has a defined possessor, otherwise {@code false}
     */
    @Override
    default boolean isBeingPossessed() { return false; }

    /**
     * Returns whether this entity is in a state ready to be possessed by the given player.
     *
     * @param player the {@link PlayerEntity} wishing to initiate the possession
     * @return {@code true} if this entity can be possessed by the given player, otherwise {@code false}
     * @implNote The default implementation checks whether it has no current possessor and it is alive
     */
    default boolean canBePossessedBy(PlayerEntity player) { return true; }

    /**
     * Sets the player possessing this entity. Called by {@link PossessionComponent#startPossessing(MobEntity)}
     *
     * @param possessor the new possessor of this entity
     */
    @ApiStatus.Internal
    default void setPossessor(@Nullable PlayerEntity possessor) {}

    /**
     * called by {@link #setPossessor(PlayerEntity)}, can be overridden to implement special behaviour
     */
    @ApiStatus.OverrideOnly
    default void onPossessorSet(@Nullable PlayerEntity possessor) {}

    /**
     * Refreshes this entity's nametag according to the value of the "requiem:showPossessorNameTag" gamerule
     *
     * @deprecated NO-OP; sync is now done through other means
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default void refreshPossessorNameTag() {}

    /**
     * Whether this entity behaves like a player with regard to hunger (hunger bar, hunger damage, food regen)
     */
    default boolean isRegularEater() { return false; }

}
