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

import baritone.api.fakeplayer.FakeServerPlayerEntity;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

/**
 * @since 1.2.0
 */
public interface RemnantComponent extends AutoSyncedComponent, ServerTickingComponent {
    ComponentKey<RemnantComponent> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "remnant"), RemnantComponent.class);

    static boolean isIncorporeal(Entity entity) {
        RemnantComponent r = KEY.getNullable(entity);
        return r != null && r.isIncorporeal();
    }

    /**
     * @since 1.4.0
     */
    static boolean isVagrant(Entity entity) {
        RemnantComponent r = KEY.getNullable(entity);
        return r != null && r.isVagrant();
    }

    /**
     * Return a player's {@link RemnantState}. The remnant state is live, and
     * every modification made to it is reflected on the player.
     *
     * @return the player's remnant state
     */
    @Contract(pure = true)
    static RemnantComponent get(PlayerEntity player) {
        return KEY.get(player);
    }

    /**
     * Make this player become the given {@link RemnantType type of remnant}.
     * <p>
     * If the given remnant type is the same as the current one, this method
     * does not have any visible effect. Otherwise, it will reset the current state,
     * replace it with a new one of the given type, and notify players of the change.
     * <p>
     * After this method has been called, the {@code RemnantType} returned by {@link #getRemnantType()}
     * will be {@code type}.
     *
     * @param type the remnant type to become
     * @see #getRemnantType()
     */
    void become(RemnantType type);

    /**
     * Make this player become the given {@link RemnantType type of remnant}.
     * <p>If the given remnant type is the same as the current one, this method
     * does not have any visible effect. Otherwise, it will reset the current state,
     * replace it with a new one of the given type, and notify players of the change.
     *
     * <p>After this method has been called, the {@code RemnantType} returned by {@link #getRemnantType()}
     * will be {@code type}.
     *
     * <p>If {@code makeChoice} is {@code true}, the player will not be shown
     * the choice dialogue on future deaths.
     *
     * @param type the remnant type to become
     * @param makeChoice whether this state change should be counted as a player action
     * @see #getRemnantType()
     */
    void become(RemnantType type, boolean makeChoice);

    @Contract(pure = true)
    RemnantType getRemnantType();

    /**
     * Return whether this player is currently incorporeal.
     * A player is considered incorporeal if its current corporeality
     * is not tangible and they have no surrogate body.
     *
     * @return true if the player is currently incorporeal, {@code false} otherwise
     */
    boolean isIncorporeal();

    @Deprecated(forRemoval = true)
    default boolean isSoul() {
        return this.isVagrant();
    }

    /**
     * Return whether this player is currently dissociated from a natural player body.
     *
     * <p>Vagrant players are invulnerable and can only interact with the world through a proxy body.
     * Being vagrant is a prerequisite to being {@linkplain #isIncorporeal() incorporeal} or to
     * {@linkplain PossessionComponent#startPossessing(MobEntity) start possessing entities}.
     *
     * @return {@code true} if the player is currently dissociated from a congruous body, {@code false} otherwise
     * @since 1.4.0
     */
    boolean isVagrant();

    @Deprecated
    default void setSoul(boolean incorporeal) {
        this.setVagrant(incorporeal);
    }

    /**
     * Set whether this player is currently dissociated from a congruous body.
     *
     * <p>This operation may fail if this state does not support the given state.
     *
     * @param vagrant {@code true} to mark this player as outside a congruous body, {@code false} to mark a merged state
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     * @see #isVagrant()
     * @see #isIncorporeal()
     * @since 1.4.0
     */
    boolean setVagrant(boolean vagrant);

    @API(status = API.Status.EXPERIMENTAL)
    boolean canRegenerateBody();

    @API(status = API.Status.EXPERIMENTAL)
    boolean canCurePossessed(LivingEntity body);

    /**
     *
     * @param body the body being used to regenerate a physical player
     */
    @API(status = API.Status.EXPERIMENTAL)
    void curePossessed(LivingEntity body);

    boolean canDissociateFrom(MobEntity possessed);

    /**
     *
     * @return {@code true} if this player can currently split into a player shell and a vagrant form
     * @param forced if {@code true}, external factors like status effects will be ignored
     * @since 2.0.0
     */
    boolean canSplitPlayer(boolean forced);

    /**
     * Attempts to split a player into a player-controlled vagrant part and an inert shell.
     *
     * <p>This operation fails if the player is currently {@linkplain #isVagrant() vagrant}.
     *
     * @param forced if {@code true}, the player will be split regardless of external factors like status effects
     * @return an {@link Optional} describing the spawned player shell, or {@link Optional#empty()} if
     * the operation failed.
     * @since 2.0.0
     */
    @API(status = API.Status.EXPERIMENTAL)
    Optional<PlayerSplitResult> splitPlayer(boolean forced);

    /**
     * Attempts to merge a player with an inert shell.
     *
     * <p>This operation fails if the player is not currently {@linkplain #isVagrant() vagrant}.
     *
     * @param shell the shell with which this player should be merged
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     */
    @API(status = API.Status.EXPERIMENTAL)
    boolean merge(FakeServerPlayerEntity shell);

    /**
     * Called when this remnant state's player is cloned
     *
     * @param original the player's clone
     * @param lossless false if the original player is dead, true otherwise
     * @since 1.3.0
     */
    void prepareRespawn(ServerPlayerEntity original, boolean lossless);

}
