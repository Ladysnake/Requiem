package ladysnake.dissolution.common.impl;

import ladysnake.dissolution.api.v1.entity.MovementAlterer;
import ladysnake.dissolution.api.v1.entity.MovementConfig;
import net.minecraft.client.network.packet.PlayerAbilitiesClientPacket;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

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
        abilities.allowFlying = this.player.isCreative() || this.player.isSpectator() || this.config.getFlightMode() != MovementConfig.FlightMode.DISABLED;
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
        if (this.config.getFlightMode() == MovementConfig.FlightMode.FORCED) {
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

    private void applyInertia(float inertia) {
        this.player.velocityX = this.player.velocityX * (1- inertia) + this.lastVelocityX * inertia;
        this.player.velocityY = this.player.velocityY * (1- inertia) + this.lastVelocityY * inertia;
        this.player.velocityZ = this.player.velocityZ * (1- inertia) + this.lastVelocityZ * inertia;
    }
}
