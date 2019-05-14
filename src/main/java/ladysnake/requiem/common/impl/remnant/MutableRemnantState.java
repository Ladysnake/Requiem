/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.impl.movement.SerializableMovementConfig;
import ladysnake.requiem.common.remnant.ClosedSpaceDetector;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import static ladysnake.requiem.common.network.RequiemNetworking.createCorporealityMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToAllTrackingIncluding;

public class MutableRemnantState implements RemnantState {
    public static final String ETHEREAL_TAG = "ethereal";

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
        return this.isSoul() && !((RequiemPlayer) player).getPossessionComponent().isPossessing();
    }

    @Override
    public boolean isSoul() {
        return this.ethereal;
    }

    @Override
    public void setSoul(boolean incorporeal) {
        if (this.ethereal != incorporeal) {
            this.ethereal = incorporeal;
            PlayerAbilities abilities = this.player.abilities;
            SerializableMovementConfig config;
            if (incorporeal) {
                config = SerializableMovementConfig.SOUL;
                abilities.invulnerable = true;
            } else {
                config = null;
                abilities.allowFlying = this.player.isCreative();
                abilities.flying &= abilities.allowFlying;
                abilities.invulnerable = this.player.isCreative();
                ((RequiemPlayer)this.player).getPossessionComponent().stopPossessing(false);
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
    public void onPlayerClone(ServerPlayerEntity clone, boolean dead) {
        ((RequiemPlayer)clone).setRemnant(true);
        RemnantState cloneState = ((RequiemPlayer) clone).getRemnantState();
        if (dead && !this.isSoul()) {
            clone.dimension = this.player.world.dimension.getType();
            ServerWorld previousWorld = clone.server.getWorld(clone.dimension);
            clone.setWorld(previousWorld);
            clone.interactionManager.setWorld(previousWorld);
            clone.copyPositionAndRotation(this.player);
            // Prevent souls from respawning in fairly bad conditions
            while(!clone.world.doesNotCollide(clone) && clone.y < 256.0D) {
                clone.setPosition(clone.x, clone.y + 1.0D, clone.z);
            }
            cloneState.setSoul(true);
        } else {
            // Copy state
            cloneState.setSoul(this.isSoul());
        }
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
