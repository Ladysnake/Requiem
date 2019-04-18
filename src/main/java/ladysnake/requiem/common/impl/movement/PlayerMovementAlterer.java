package ladysnake.requiem.common.impl.movement;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import net.minecraft.client.network.packet.PlayerAbilitiesS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.WaterCreatureEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import static ladysnake.requiem.api.v1.entity.MovementConfig.MovementMode.*;

public class PlayerMovementAlterer implements MovementAlterer {
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
        if (this.config == null) {
            return;
        }
        PlayerAbilities abilities = this.player.abilities;
        abilities.allowFlying = player.isCreative() || player.isSpectator() || getActualFlightMode(config, player) != DISABLED;
        abilities.flying &= abilities.allowFlying;
        if (player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).networkHandler != null) {
            ((ServerPlayerEntity) player).networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(abilities));
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
        LivingEntity possessed = (LivingEntity) ((RequiemPlayer)player).getPossessionComponent().getPossessedEntity();
        return possessed == null ? player : possessed;
    }

    private static MovementConfig.MovementMode getActualSwimMode(MovementConfig config, Entity entity) {
        if (config.getSwimMode() == UNSPECIFIED) {
            return entity instanceof WaterCreatureEntity ? FORCED : DISABLED;
        }
        return config.getSwimMode();
    }

    private static MovementConfig.MovementMode getActualFlightMode(MovementConfig config, Entity entity) {
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
