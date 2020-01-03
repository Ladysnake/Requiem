/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
 */
package ladysnake.requiem.common.impl.remnant;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.impl.movement.SerializableMovementConfig;
import ladysnake.requiem.common.remnant.ClosedSpaceDetector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import static ladysnake.requiem.common.network.RequiemNetworking.createCorporealityMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToAllTrackingIncluding;

public class MutableRemnantState implements RemnantState {
    public static final String ETHEREAL_TAG = "ethereal";
    public static final AbilitySource SOUL_STATE = Pal.getAbilitySource(Requiem.id("soul_state"));

    private final RemnantType type;
    protected final PlayerEntity player;
    protected final ClosedSpaceDetector closedSpaceDetector;
    protected boolean ethereal;
    private boolean lastTickIncorporeal;

    public MutableRemnantState(RemnantType type, PlayerEntity player) {
        this.type = type;
        this.player = player;
        this.closedSpaceDetector = new ClosedSpaceDetector(player);
    }

    @Override
    public boolean isIncorporeal() {
        return this.isSoul() && !((RequiemPlayer) player).asPossessor().isPossessing();
    }

    @Override
    public boolean isSoul() {
        return this.ethereal;
    }

    @Override
    public void setSoul(boolean incorporeal) {
        if (this.ethereal != incorporeal) {
            this.ethereal = incorporeal;
            SerializableMovementConfig config;
            if (incorporeal) {
                config = SerializableMovementConfig.SOUL;
                SOUL_STATE.grantTo(player, VanillaAbilities.INVULNERABLE);
            } else {
                config = null;
                SOUL_STATE.revokeFrom(player, VanillaAbilities.INVULNERABLE);
                ((RequiemPlayer)this.player).asPossessor().stopPossessing(false);
            }
            ((RequiemPlayer)this.player).getMovementAlterer().setConfig(config);
            if (!this.player.world.isClient) {
                // Synchronizes with all players tracking the owner
                sendToAllTrackingIncluding(this.player, createCorporealityMessage(this.player));
            }
        }
    }

    @Override
    public void update() {
        boolean incorporeal = this.isIncorporeal();
        if (incorporeal) {
            if (!player.world.isClient) {
                if (!this.lastTickIncorporeal) {
                    this.closedSpaceDetector.reset(false);
                }
                this.closedSpaceDetector.tick();
            }
        }
        this.lastTickIncorporeal = incorporeal;
    }

    @Override
    public void copyFrom(ServerPlayerEntity original, boolean lossless) {
        RemnantState ogState = RequiemPlayer.from(original).asRemnant();
        if (lossless || ogState.isSoul()) {
            // Copy state
            this.setSoul(ogState.isSoul());
        } else {
            this.setSoul(true);
            this.copyGlobalPos(original);
        }
    }

    protected void copyGlobalPos(ServerPlayerEntity original) {
        ServerPlayerEntity clone = (ServerPlayerEntity) this.player;
        clone.dimension = original.world.dimension.getType();
        ServerWorld previousWorld = clone.server.getWorld(clone.dimension);
        clone.setWorld(previousWorld);
        clone.interactionManager.setWorld(previousWorld);
        clone.copyPositionAndRotation(original);
    }

    @Override
    public RemnantType getType() {
        return this.type;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putBoolean(ETHEREAL_TAG, this.isSoul());
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.setSoul(tag.getBoolean(ETHEREAL_TAG));
    }
}
