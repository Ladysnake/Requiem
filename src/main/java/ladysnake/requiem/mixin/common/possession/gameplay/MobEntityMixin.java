/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.mixin.common.possession.gameplay;

import ladysnake.requiem.api.v1.event.minecraft.JumpingMountEvents;
import ladysnake.requiem.api.v1.event.minecraft.MobTravelRidingCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionEvents;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.common.possession.ExternalJumpingMount;
import ladysnake.requiem.core.util.DetectionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntityMixin implements Possessable {
    private @Nullable Float requiem$previousStepHeight;

    @Shadow
    public abstract void setMovementSpeed(float movementSpeed);

    public MobEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected Vec3d requiem$travelStart(Vec3d movementInput) {
        if (this.requiem$previousStepHeight != null) {
            this.stepHeight = this.requiem$previousStepHeight;
            this.requiem$previousStepHeight = null;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        if (JumpingMountEvents.MOUNT_CHECK.invoker().getJumpingMount(self) instanceof ExternalJumpingMount jumpingMount) {
            jumpingMount.attemptJump();
        }

        // Straight up copied from HorseBaseEntity#travel
        // Also replaces canBeControlledByRider and getPrimaryPassenger with more generic alternatives
        if (this.isAlive() && this.hasPassengers()) {
            Entity primaryPassenger = this.getPrimaryPassenger();
            Entity passenger = primaryPassenger != null ? primaryPassenger : this.getFirstPassenger();

            if (!(passenger instanceof LivingEntity livingEntity) || !MobTravelRidingCallback.EVENT.invoker().canBeControlled((MobEntity) (Object) this, livingEntity)) {
                return movementInput;
            }

            this.setYaw(livingEntity.getYaw());
            this.prevYaw = this.getYaw();
            this.setPitch(livingEntity.getPitch() * 0.5F);
            this.setRotation(this.getYaw(), this.getPitch());
            this.bodyYaw = this.getYaw();
            this.headYaw = this.bodyYaw;

            if (this.stepHeight < 1.0F) {
                this.requiem$previousStepHeight = this.stepHeight;
                this.stepHeight = 1.0F;
            }

            float sidewaysSpeed = livingEntity.sidewaysSpeed * 0.5F;
            float forwardSpeed = livingEntity.forwardSpeed;
            if (forwardSpeed <= 0.0F) {
                forwardSpeed *= 0.25F;
            }

            this.requiem$setAirStrafingSpeed(this.getMovementSpeed() * 0.1F);
            // isLogicalSideForUpdatingMovement but inlined
            if (passenger instanceof PlayerEntity player && player.isMainPlayer() || !this.world.isClient()) {
                float speed;
                if (this instanceof ItemSteerable steerable) {
                    speed = steerable.getSaddledSpeed();
                } else {
                    speed = (float) this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                }
                this.setMovementSpeed(speed);
                return new Vec3d(sidewaysSpeed, movementInput.y, forwardSpeed);
            } else if (livingEntity instanceof PlayerEntity) {
                this.setVelocity(Vec3d.ZERO);
            }
        }
        return movementInput;
    }

    @Override
    protected void requiem$travelEnd(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        this.updateLimbs(self, false);

        if (this.onGround && JumpingMountEvents.MOUNT_CHECK.invoker().getJumpingMount(self) instanceof ExternalJumpingMount jumpingMount) {
            jumpingMount.endJump();
        }
    }

    @Override
    public void requiem$pushed(Entity pushed, CallbackInfo ci) {
        MobEntity self = (MobEntity) (Object) this;
        DetectionHelper.attemptDetection(self, pushed, PossessionEvents.DetectionAttempt.DetectionReason.BUMP);
    }

    @Override
    public void requiem$damaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof MobEntity attacker) {
            MobEntity self = (MobEntity) (Object) this;
            DetectionHelper.attemptDetection(self, attacker, PossessionEvents.DetectionAttempt.DetectionReason.ATTACKING);
            DetectionHelper.attemptDetection(attacker, self, PossessionEvents.DetectionAttempt.DetectionReason.ATTACKED);
        }
    }
}
