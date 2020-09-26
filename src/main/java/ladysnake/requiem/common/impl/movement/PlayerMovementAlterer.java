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
package ladysnake.requiem.common.impl.movement;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.mixin.common.access.EntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import static ladysnake.requiem.api.v1.entity.MovementConfig.MovementMode.*;

public class PlayerMovementAlterer implements MovementAlterer {
    public static final AbilitySource MOVEMENT_ALTERER_ABILITIES = Pal.getAbilitySource(Requiem.id("movement_alterer"));
    public static final int SYNC_NO_CLIP = 1;
    @Nullable
    private MovementConfig config;
    private final PlayerEntity player;
    private Vec3d lastVelocity = Vec3d.ZERO;
    private int ticksAgainstWall = 0;
    private boolean noClipping = false;

    public PlayerMovementAlterer(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void setConfig(@CheckForNull MovementConfig config) {
        this.config = config;
        this.applyConfig();
    }

    @Override
    public void applyConfig() {
        if (!this.player.world.isClient) {
            if (getActualFlightMode(config, player) == DISABLED) {
                Pal.revokeAbility(player, VanillaAbilities.ALLOW_FLYING, MOVEMENT_ALTERER_ABILITIES);
            } else {
                Pal.grantAbility(player, VanillaAbilities.ALLOW_FLYING, MOVEMENT_ALTERER_ABILITIES);
            }
            this.hugWall(false);
        }
    }

    @Override
    public float getSwimmingAcceleration(float baseAcceleration) {
        if (this.config != null && getActualSwimMode(this.config, getPlayerOrPossessed(player)) == FORCED) {
            return 0.96F;
        }
        return baseAcceleration;
    }

    @Override
    public boolean canClimbWalls() {
        return this.config != null && this.config.canClimbWalls();
    }

    @Override
    public void clientTick() {
        if (this.config != null && !this.player.noClip && this.player == MinecraftClient.getInstance().player && this.config.canPhaseThroughWalls()) {
            float verticalMovement = ((ClientPlayerEntity) this.player).input.jumping ? 1 : 0;
            Vec3d movement = EntityAccessor.invokeMovementInputToVelocity(new Vec3d(this.player.sidewaysSpeed, verticalMovement, this.player.forwardSpeed), 1, this.player.yaw);
            Vec3d adjusted = ((EntityAccessor)this.player).invokeAdjustMovementForCollisions(movement);
            // 10.0 is a magic constant that corresponds to mostly blocked movement
            if (movement.length() / adjusted.length() > 10.0) {
                RequiemNetworking.sendHugWallMessage(true);
            } else if (this.ticksAgainstWall > 0) {
                RequiemNetworking.sendHugWallMessage(false);
            }
        }
        if (this.ticksAgainstWall < 0) {
            this.ticksAgainstWall++;
        } else if (this.noClipping && this.player.world.isSpaceEmpty(this.player)) {
            RequiemNetworking.sendHugWallMessage(false);
        }
        this.tick();
    }

    @Override
    public void tick() {
        if (this.config == null) {
            return;
        }
        MovementConfig.MovementMode swimMode = getActualSwimMode(config, getPlayerOrPossessed(player));
        if (swimMode == FORCED) {
            player.setSwimming(true);
        } else if (swimMode == DISABLED) {
            player.setSwimming(false);
        }
        if (getActualFlightMode(config, getPlayerOrPossessed(player)) == FORCED || this.noClipping) {
            this.player.abilities.flying = true;
        }
        if (this.player.isOnGround() && config.shouldFlopOnLand() && this.player.world.getBlockState(this.player.getBlockPos()).isAir()) {
            this.player.jump();
        }
        Vec3d velocity = this.player.getVelocity();
        velocity = applyGravity(velocity, this.config.getAddedGravity());
        velocity = applyFallSpeedModifier(velocity, this.config.getFallSpeedModifier());
        velocity = applyInertia(velocity, this.config.getInertia());
        this.player.setVelocity(velocity);
        this.lastVelocity = velocity;
    }

    @Override
    public void hugWall(boolean hugging) {
        if (this.config != null && this.config.canPhaseThroughWalls() && hugging) {
            this.ticksAgainstWall++;

            if (this.ticksAgainstWall > 60 && !this.noClipping) {
                this.noClipping = true;
                this.ticksAgainstWall = 0;
            }
        } else {
            this.ticksAgainstWall = 0;
            this.noClipping = false;
        }
        KEY.sync(this.player, SYNC_NO_CLIP);
    }

    @Override
    public boolean isNoClipping() {
        return this.noClipping;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player, int syncOp) {
        return player == this.player && syncOp == SYNC_NO_CLIP;
    }

    @Override
    public void writeToPacket(PacketByteBuf buf, ServerPlayerEntity recipient, int syncOp) {
        buf.writeByte(syncOp);
        if (syncOp == SYNC_NO_CLIP) {
            buf.writeBoolean(this.noClipping);
        }
    }

    @Override
    public void readFromPacket(PacketByteBuf buf) {
        byte syncOp = buf.readByte();
        if (syncOp == SYNC_NO_CLIP) {
            this.noClipping = buf.readBoolean();
            this.ticksAgainstWall = -5;
        }
    }

    private Vec3d applyGravity(Vec3d velocity, float addedGravity) {
        return velocity.subtract(0, addedGravity, 0);
    }

    private Vec3d applyFallSpeedModifier(Vec3d velocity, float fallSpeedModifier) {
        if (!this.player.isOnGround() && velocity.y < 0) {
            velocity = velocity.multiply(1.0, fallSpeedModifier, 1.0);
        }
        return velocity;
    }

    private static LivingEntity getPlayerOrPossessed(PlayerEntity player) {
        LivingEntity possessed = PossessionComponent.get(player).getPossessedEntity();
        return possessed == null ? player : possessed;
    }

    private static MovementConfig.MovementMode getActualSwimMode(MovementConfig config, Entity entity) {
        if (config.getSwimMode() == UNSPECIFIED) {
            return entity instanceof WaterCreatureEntity ? FORCED : DISABLED;
        }
        return config.getSwimMode();
    }

    private static MovementConfig.MovementMode getActualFlightMode(@Nullable MovementConfig config, Entity entity) {
        if (config == null) {
            return DISABLED;
        }
        if (config.getFlightMode() == UNSPECIFIED) {
            return entity instanceof FlyingEntity ? FORCED : DISABLED;
        }
        return config.getFlightMode();
    }

    private Vec3d applyInertia(Vec3d velocity, float inertia) {
        // velocity = velocity * (1 - inertia) + lastVelocity * inertia
        return velocity.multiply(1 - inertia).add(this.lastVelocity.multiply(inertia));
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        // NO-OP
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        // NO-OP
    }
}
