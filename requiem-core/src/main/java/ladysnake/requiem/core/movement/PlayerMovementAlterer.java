/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.core.movement;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import ladysnake.requiem.api.v1.entity.movement.SwimMode;
import ladysnake.requiem.api.v1.entity.movement.WalkMode;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.RequiemCoreNetworking;
import ladysnake.requiem.core.mixin.access.EntityAccessor;
import ladysnake.requiem.core.tag.RequiemCoreEntityTags;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.ToDoubleFunction;

import static ladysnake.requiem.api.v1.entity.MovementConfig.MovementMode.*;

public abstract class PlayerMovementAlterer implements MovementAlterer {
    public static final AbilitySource MOVEMENT_ALTERER_ABILITIES = Pal.getAbilitySource(RequiemCore.id("movement_alterer"), AbilitySource.FREE);
    public static final byte DEFAULT_SYNC = 0;
    public static final byte SYNC_NO_CLIP = 1;
    public static final byte SYNC_PHASING_PARTICLES = 2;
    public static final UUID SPEED_MODIFIER_UUID = UUID.fromString("3708adba-b37f-413f-8b66-62e05330c7da");
    public static final UUID WATER_SPEED_MODIFIER_UUID = UUID.fromString("0e602dd1-2672-4179-852e-2a26d1579df4");
    public static final int TICKS_BEFORE_PHASING = 60;

    @Nullable
    private MovementConfig config;
    protected final PlayerEntity player;
    private Vec3d lastVelocity = Vec3d.ZERO;
    private int ticksAgainstWall = 0;
    private boolean noClipping = false;

    private boolean underwaterJumpAscending;
    private double underwaterJumpStartY;

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
            this.underwaterJumpAscending = false;
            this.updateSpeedModifier(getCurrentBody(this.player), SPEED_MODIFIER_UUID, MovementConfig::getLandedSpeedModifier, true);
        }
    }

    private void updateSpeedModifier(LivingEntity currentBody, UUID speedModifierUuid, ToDoubleFunction<MovementConfig> property, boolean shouldApplyModifier) {
        EntityAttributeInstance speedAttribute = currentBody.getAttributes().m_rkfdyugp(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(speedModifierUuid);
            if (shouldApplyModifier && config != null && property.applyAsDouble(config) != 1.0) {
                speedAttribute.addTemporaryModifier(new EntityAttributeModifier(speedModifierUuid, "Requiem altered movement", property.applyAsDouble(config), EntityAttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }

    @CheckEnv(Env.CLIENT)
    @Override
    public void alterControls() {
        if (this.config != null
            && getActualSwimMode(this.config, getCurrentBody(this.player)) == SwimMode.FLOATING
            && isInFluid(this.player)
            && this.player.getRandom().nextFloat() < 0.8F
        ) {
            this.player.setJumping(true);
        }
    }

    // Based on SwimGoal#canStart
    private static boolean isInFluid(LivingEntity entity) {
        return entity.isTouchingWater() && entity.getFluidHeight(FluidTags.WATER) > entity.getSwimHeight() || entity.isInLava();
    }

    @Override
    public float getSwimmingAcceleration(float baseAcceleration) {
        if (this.config != null && getActualSwimMode(this.config, getCurrentBody(player)) == SwimMode.FORCED) {
            return 0.96F;
        }
        return baseAcceleration;
    }

    @Override
    public double getSwimmingUpwardsVelocity(double baseUpwardsVelocity) {
        if (this.config != null && getActualSwimMode(this.config, getCurrentBody(player)) == SwimMode.SINKING) {
            double y = this.player.getY();
            if (this.player.isOnGround()) {    // starting the jump
                this.underwaterJumpStartY = y;
                this.underwaterJumpAscending = true;
            } else if (this.underwaterJumpAscending && y > underwaterJumpStartY + (this.player.isTouchingWater() ? 0.8 : 1.0)) {    // reaching peak
                this.underwaterJumpAscending = false;
            } else if (!this.underwaterJumpAscending) { // sinking again
                return 0;
            }
        }
        return baseUpwardsVelocity;
    }

    @Override
    public boolean canClimbWalls() {
        return this.config != null && this.config.canClimbWalls();
    }

    @Override
    public void clientTick() {
        ClientPlayerEntity mainPlayer = MinecraftClient.getInstance().player;
        if (this.config != null && this.player == mainPlayer) {
            if (this.config.canPhaseThroughWalls()) {
                if (!this.noClipping && !this.player.noClip) {
                    Vec3d movement = getIntendedMovement(this.player);
                    Vec3d adjusted = ((EntityAccessor) this.player).requiem$invokeAdjustMovementForCollisions(movement);
                    // 10.0 is an arbitrary constant that corresponds to mostly blocked movement
                    if (movement.length() / adjusted.length() > 10.0 && this.player.getRotationVector().dotProduct(movement.normalize()) > 0.5) {
                        this.ticksAgainstWall++;
                        RequiemCoreNetworking.sendHugWallMessage(true);
                    } else if (this.ticksAgainstWall > 0) {
                        RequiemCoreNetworking.sendHugWallMessage(false);
                    }
                } else if (this.noClipping && this.player.getRandom().nextFloat() > 0.8f) {
                    this.playPhaseEffects();
                }
            }
            if (getActualWalkMode(this.config, getCurrentBody(player)) == WalkMode.JUMPY && this.player.isOnGround() && !getIntendedMovement(player).equals(Vec3d.ZERO)) {
                this.player.jump();
            }
            if (this.underwaterJumpAscending && !mainPlayer.input.jumping) {
                this.underwaterJumpAscending = false;
            }
        }
        if (this.ticksAgainstWall < 0) {
            this.ticksAgainstWall++;
        } else if (this.noClipping) {
            this.noClipping = false;    // disable to check whether there really are blocks
            if (this.player.world.isSpaceEmpty(this.player)) {
                RequiemCoreNetworking.sendHugWallMessage(false);
            }
            this.noClipping = true;
        }
        this.tick();
    }

    @NotNull
    protected static Vec3d getIntendedMovement(PlayerEntity player) {
        if (player instanceof ClientPlayerEntity) {
            float verticalMovement = (((ClientPlayerEntity) player).input.jumping ? 1 : 0) - (((ClientPlayerEntity) player).input.sneaking ? 1 : 0);
            return EntityAccessor.requiem$invokeMovementInputToVelocity(new Vec3d(player.sidewaysSpeed, verticalMovement, player.forwardSpeed), 1, player.getYaw());
        } else {
            return Vec3d.ZERO;
        }
    }

    @Override
    public void serverTick() {
        this.updateSpeedModifier(
            getCurrentBody(this.player),
            WATER_SPEED_MODIFIER_UUID,
            MovementConfig::getWaterSpeedModifier,
            player.isTouchingWater()
        );
        this.tick();
    }

    @Override
    public void tick() {
        if (this.config == null) {
            return;
        }
        LivingEntity body = getCurrentBody(player);
        updateSwimming();

        if (getActualFlightMode(config, body) == FORCED || this.noClipping) {
            this.player.getAbilities().flying = true;
        }
        if (this.player.isOnGround() && shouldActuallyFlopOnLand(config, body) && this.player.world.getFluidState(this.player.getBlockPos()).isEmpty()) {
            this.player.jump();
        }
        Vec3d velocity = this.player.getVelocity();
        velocity = applyGravity(velocity, this.config.getAddedGravity());
        velocity = applyFallSpeedModifier(velocity, this.config.getFallSpeedModifier());
        velocity = applyInertia(velocity, this.config.getInertia());
        this.player.setVelocity(velocity);
        this.lastVelocity = velocity;
    }

    private boolean shouldActuallyFlopOnLand(MovementConfig config, LivingEntity body) {
        TriState value = config.shouldFlopOnLand();
        if (value != TriState.DEFAULT) {
            return value.get();
        }
        return body instanceof FishEntity;
    }

    @Override
    public void updateSwimming() {
        if (config != null) {
            SwimMode swimMode = getActualSwimMode(config, getCurrentBody(player));
            if (swimMode.sprintSwims() != TriState.DEFAULT) {
                player.setSwimming(swimMode.sprintSwims().get());
            }
        }
    }

    @Override
    public boolean disablesSwimming() {
        return config != null && !getActualSwimMode(config, getCurrentBody(player)).sprintSwims().orElse(true);
    }

    @Override
    public void hugWall(boolean hugging) {
        if (this.config != null && this.config.canPhaseThroughWalls() && hugging) {
            this.ticksAgainstWall++;

            if (this.ticksAgainstWall > TICKS_BEFORE_PHASING && !this.noClipping) {
                this.noClipping = true;
                this.ticksAgainstWall = 0;
                KEY.sync(this.player, (buf, player) -> writeToPacket(buf, SYNC_NO_CLIP), player -> shouldSyncWith(player, SYNC_NO_CLIP));
            } else if (this.ticksAgainstWall % 10 == 0) {
                KEY.sync(this.player, (buf, player) -> writeToPacket(buf, SYNC_PHASING_PARTICLES), player -> shouldSyncWith(player, SYNC_PHASING_PARTICLES));
            }
        } else {
            this.ticksAgainstWall = 0;
            this.noClipping = false;
            KEY.sync(this.player, (buf, player) -> writeToPacket(buf, SYNC_NO_CLIP), player -> shouldSyncWith(player, SYNC_NO_CLIP));
        }
    }

    @Override
    public boolean isNoClipping() {
        return this.noClipping;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return this.shouldSyncWith(player, 0);
    }

    private boolean shouldSyncWith(ServerPlayerEntity player, int syncOp) {
        return (
            syncOp == SYNC_NO_CLIP && player == this.player
        ) || (
            syncOp == SYNC_PHASING_PARTICLES
                && RemnantComponent.get(player).getRemnantType().isDemon()
                && player.squaredDistanceTo(this.player) < 16 * 16
        );
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        this.writeToPacket(buf, DEFAULT_SYNC);
    }

    private void writeToPacket(PacketByteBuf buf, int syncOp) {
        buf.writeByte(syncOp);
        if (syncOp == SYNC_NO_CLIP) {
            buf.writeBoolean(this.noClipping);
        }
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        byte syncOp = buf.readByte();
        if (syncOp == SYNC_NO_CLIP) {
            this.noClipping = buf.readBoolean();
            this.ticksAgainstWall = this.noClipping ? -5 : 0;
        } else if (syncOp == SYNC_PHASING_PARTICLES) {
            this.playPhaseEffects();
        }
    }

    @CheckEnv(Env.CLIENT)
    protected abstract void playPhaseEffects();

    private Vec3d applyGravity(Vec3d velocity, float addedGravity) {
        return velocity.subtract(0, addedGravity, 0);
    }

    private Vec3d applyFallSpeedModifier(Vec3d velocity, float fallSpeedModifier) {
        if (!this.player.isOnGround() && velocity.y < 0) {
            velocity = velocity.multiply(1.0, fallSpeedModifier, 1.0);
        }
        return velocity;
    }

    private static LivingEntity getCurrentBody(PlayerEntity player) {
        LivingEntity possessed = PossessionComponent.get(player).getHost();
        return possessed == null ? player : possessed;
    }

    @SuppressWarnings("deprecation")    // backwards compatibility
    private static boolean shouldActuallySinkInWater(MovementConfig config, Entity entity) {
        if (config.shouldSinkInWater() == TriState.DEFAULT) {
            EntityType<?> type = entity.getType();
            return type.isIn(RequiemCoreEntityTags.GOLEMS) || entity instanceof LivingEntity && ((LivingEntity) entity).isUndead();
        }
        return config.shouldSinkInWater().get();
    }

    private static WalkMode getActualWalkMode(MovementConfig config, LivingEntity body) {
        WalkMode walkMode = config.getWalkMode();
        if (walkMode == WalkMode.UNSPECIFIED) {
            return body instanceof SlimeEntity ? WalkMode.JUMPY : WalkMode.NORMAL;
        }
        return walkMode;
    }

    private static SwimMode getActualSwimMode(MovementConfig config, Entity entity) {
        if (config.getSwimMode() == SwimMode.UNSPECIFIED) {
            if (shouldActuallySinkInWater(config, entity)) {
                return SwimMode.SINKING;
            } else if (entity instanceof WaterCreatureEntity) {
                return SwimMode.FORCED;
            } else if (entity instanceof SlimeEntity) {
                return SwimMode.FLOATING;
            }
            return SwimMode.DISABLED;
        }
        return config.getSwimMode();
    }

    private static MovementConfig.MovementMode getActualFlightMode(@Nullable MovementConfig config, Entity entity) {
        if (config == null) {
            return DISABLED;
        }
        if (config.getFlightMode() == UNSPECIFIED) {
            return (entity instanceof FlyingEntity
                || entity instanceof Flutterer
            ) ? FORCED : DISABLED;
        }
        return config.getFlightMode();
    }

    private Vec3d applyInertia(Vec3d velocity, float inertia) {
        // velocity = velocity * (1 - inertia) + lastVelocity * inertia
        return velocity.multiply(1 - inertia).add(this.lastVelocity.multiply(inertia));
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        // NO-OP
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        // NO-OP
    }
}
