package ladysnake.dissolution.common.impl.movement;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.entity.MovementAlterer;
import ladysnake.dissolution.api.v1.entity.MovementConfig;
import net.minecraft.client.network.packet.PlayerAbilitiesClientPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.WaterCreatureEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import static ladysnake.dissolution.api.v1.entity.MovementConfig.MovementMode.*;

public class PlayerMovementAlterer implements MovementAlterer {
    @Nullable
    private MovementConfig config;
    private PlayerEntity player;
    private double lastVelocityX;
    private double lastVelocityY;
    private double lastVelocityZ;

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
        // method_7325 == isSpectator
        abilities.allowFlying = player.isCreative() || player.method_7325() || getActualFlightMode(config, player) != DISABLED;
        abilities.flying &= abilities.allowFlying;
        if (player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).networkHandler != null) {
            ((ServerPlayerEntity) player).networkHandler.sendPacket(new PlayerAbilitiesClientPacket(abilities));
        }
    }

    @Override
    public void update() {
        if (this.config == null) {
            return;
        }
        MovementConfig.MovementMode swimMode = getActualSwimMode(config, getPlayerOrPossessed(player));
        if (swimMode == FORCED) {
            player.method_5796(true);
        } else if (swimMode == DISABLED) {
            player.method_5796(false);
        }
        if (getActualFlightMode(config, getPlayerOrPossessed(player)) == FORCED) {
            this.player.abilities.flying = true;
        }
        this.player.velocityY -= this.config.getAddedGravity();
        if (!this.player.onGround && this.player.velocityY < 0) {
            this.player.velocityY *= this.config.getFallSpeedModifier();
        }
        applyInertia(this.config.getInertia());
        this.lastVelocityX = this.player.velocityX;
        this.lastVelocityY = this.player.velocityY;
        this.lastVelocityZ = this.player.velocityZ;
    }

    private static LivingEntity getPlayerOrPossessed(PlayerEntity player) {
        LivingEntity possessed = (LivingEntity) ((DissolutionPlayer)player).getPossessionComponent().getPossessedEntity();
        return possessed == null ? player : possessed;
    }

    private static MovementConfig.MovementMode getActualSwimMode(MovementConfig config, Entity entity) {
        if (config.getSwimMode() == UNSPECIFIED) {
            return entity instanceof WaterCreatureEntity ? FORCED : DISABLED;
        }
        return config.getSwimMode();
    }

    private MovementConfig.MovementMode getActualFlightMode(MovementConfig config, Entity entity) {
        if (config.getFlightMode() == UNSPECIFIED) {
            return entity instanceof FlyingEntity ? FORCED : DISABLED;
        }
        return config.getFlightMode();
    }

    private void applyInertia(float inertia) {
        this.player.velocityX = this.player.velocityX * (1- inertia) + this.lastVelocityX * inertia;
        this.player.velocityY = this.player.velocityY * (1- inertia) + this.lastVelocityY * inertia;
        this.player.velocityZ = this.player.velocityZ * (1- inertia) + this.lastVelocityZ * inertia;
    }
}
