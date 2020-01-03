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
package ladysnake.requiem.common.impl.movement;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import static ladysnake.requiem.api.v1.entity.MovementConfig.MovementMode.*;

public class PlayerMovementAlterer implements MovementAlterer {
    public static final AbilitySource MOVEMENT_ALTERER_ABILITIES = Pal.getAbilitySource(Requiem.id("movement_alterer"));
    @Nullable
    private MovementConfig config;
    private final PlayerEntity player;
    private Vec3d lastVelocity = Vec3d.ZERO;

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
        if (getActualFlightMode(config, player) == DISABLED) {
            MOVEMENT_ALTERER_ABILITIES.revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
        } else {
            MOVEMENT_ALTERER_ABILITIES.grantTo(player, VanillaAbilities.ALLOW_FLYING);
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
    public void update() {
        if (this.config == null) {
            return;
        }
        MovementConfig.MovementMode swimMode = getActualSwimMode(config, getPlayerOrPossessed(player));
        if (swimMode == FORCED) {
            player.setSwimming(true);
        } else if (swimMode == DISABLED) {
            player.setSwimming(false);
        }
        if (getActualFlightMode(config, getPlayerOrPossessed(player)) == FORCED) {
            this.player.abilities.flying = true;
        }
        if (this.player.onGround && config.shouldFlopOnLand() && this.player.world.getBlockState(this.player.getBlockPos()).isAir()) {
            this.player.jump();
        }
        Vec3d velocity = this.player.getVelocity();
        velocity = applyGravity(velocity, this.config.getAddedGravity());
        velocity = applyFallSpeedModifier(velocity, this.config.getFallSpeedModifier());
        velocity = applyInertia(velocity, this.config.getInertia());
        this.player.setVelocity(velocity);
        this.lastVelocity = velocity;
    }

    private Vec3d applyGravity(Vec3d velocity, float addedGravity) {
        return velocity.subtract(0, addedGravity, 0);
    }

    private Vec3d applyFallSpeedModifier(Vec3d velocity, float fallSpeedModifier) {
        if (!this.player.onGround && velocity.y < 0) {
            velocity = velocity.multiply(1.0, fallSpeedModifier, 1.0);
        }
        return velocity;
    }

    private static LivingEntity getPlayerOrPossessed(PlayerEntity player) {
        LivingEntity possessed = ((RequiemPlayer)player).asPossessor().getPossessedEntity();
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
}
