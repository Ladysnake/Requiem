package ladysnake.dissolution.common.impl;

import ladysnake.dissolution.api.v1.entity.MovementAlterer;
import ladysnake.dissolution.api.v1.entity.MovementConfig;
import net.minecraft.client.network.packet.PlayerAbilitiesClientPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

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

    @Nullable
    @Override
    public MovementConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(MovementConfig config) {
        this.config = config;
    }

    @Override
    public void onMotionStateChanged() {
        if (this.config == null) {
            return;
        }
        if (this.config.getFlightMode() != MovementConfig.FlightMode.DISABLED) {
            this.player.abilities.allowFlying = true;
            if (!this.player.world.isClient) {
                ((ServerPlayerEntity) player).networkHandler.sendPacket(new PlayerAbilitiesClientPacket(player.abilities));
            }
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
