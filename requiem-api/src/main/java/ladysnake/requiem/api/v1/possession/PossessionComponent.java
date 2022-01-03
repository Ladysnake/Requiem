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

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link PossessionComponent} handles a player's possession status.
 */
public interface PossessionComponent extends AutoSyncedComponent, ServerTickingComponent {
    ComponentKey<PossessionComponent> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "possessor"), PossessionComponent.class);

    /**
     * Return a player's {@link PossessionComponent}. The possession component is
     * live, and every modification made to it is reflected on the player.
     *
     * @return the player's possession component
     * @since 1.2.0
     */
    @Contract(pure = true)
    static PossessionComponent get(PlayerEntity player) {
        return KEY.get(player);
    }

    static @Nullable MobEntity getHost(Entity possessor) {
        PossessionComponent p = KEY.getNullable(possessor);
        return p == null ? null : p.getHost();
    }

    /**
     * Attempts to start possessing a mob.
     * <p>
     * Starting possession sets internal state for both the {@link Possessable mob} and the
     * {@link RequiemPlayer player} represented by this component.
     * It will also make any necessary change to the global game state (eg. teleporting the
     * player to the possessed mob, or transferring equipment).
     * <p>
     * This method returns <code>true</code> if the <code>mob</code> has been successfully
     * possessed.
     * <p>
     * After this method returns, and if the attempt is successful, further calls to
     * {@link #getHost()} will return <code>mob</code> until either {@link #stopPossessing()} is called,
     * or <code>mob</code> cannot be found, whichever happens first. Likewise, calling {@link Possessable#getPossessor()}
     * on <code>mob</code> will return the player represented by this component.
     * <p>
     * Calling this method is equivalent to calling <code>startPossessing(mob, false)</code>.
     *
     * @param mob      a mob to possess
     * @return <code>true</code> if the attempt succeeded, <code>false</code> otherwise
     * @see #startPossessing(MobEntity, boolean)
     */
    default boolean startPossessing(MobEntity mob) {
        return startPossessing(mob, false);
    }

    /**
     * Attempts to start possessing a mob.
     *
     * <p>Starting possession sets internal state for both the {@link Possessable mob} and the
     * {@link RequiemPlayer player} represented by this component.
     * It will also make any necessary change to the global game state (eg. teleporting the
     * player to the possessed mob, or transferring equipment).
     *
     * <p>This method returns <code>true</code> if the <code>mob</code> has been successfully
     * possessed.
     *
     * <p>If <code>simulate</code> is true, the attempt is simulated.
     * When the attempt is simulated, the state of the game is not altered by this method's execution.
     * This means that this method is effectively pure during simulated possession attempts,
     * in the following sense:
     * <em>If its return value is not used, removing its invocation won't
     * affect program state and change the semantics. Exception throwing is not considered to be a side effect.</em>
     *
     * <p>After this method returns, and if the attempt is successful and not simulated, further calls to
     * {@link #getHost()} will return <code>mob</code> until either {@link #stopPossessing()} is called,
     * or <code>mob</code> cannot be found, whichever happens first. Likewise, calling {@link Possessable#getPossessor()}
     * on <code>mob</code> will return the player represented by this component.
     *
     * @param mob      a mob to possess
     * @param simulate whether the possession attempt should only be simulated
     * @return <code>true</code> if the attempt succeeded, <code>false</code> otherwise
     *
     * @implSpec implementations of this method should call
     * {@link PossessionStartCallback#onPossessionAttempted(MobEntity, PlayerEntity, boolean)} before
     * proceeding with the actual possession.
     *
     * @see PossessionStartCallback
     */
    boolean startPossessing(MobEntity mob, boolean simulate);

    /**
     * Stops an ongoing possession.
     * Equipment will be transferred if the player is not in creative.
     */
    void stopPossessing();

    /**
     * Stops an ongoing possession.
     * @param transfer whether equipment should be transferred to the entity
     */
    void stopPossessing(boolean transfer);

    @Deprecated
    default @Nullable MobEntity getPossessedEntity() {
        return getHost();
    }

    /**
     * @return the entity that is currently being possessed, or {@code null} if no possession
     * is currently taking place
     */
    @Nullable MobEntity getHost();

    boolean isPossessionOngoing();

    boolean canBeCured(ItemStack cure);

    void startCuring();

    boolean isCuring();

}
