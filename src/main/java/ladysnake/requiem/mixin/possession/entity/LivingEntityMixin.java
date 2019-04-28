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
package ladysnake.requiem.mixin.possession.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.common.entity.ai.attribute.AttributeHelper;
import ladysnake.requiem.common.entity.ai.attribute.CooldownStrengthAttribute;
import ladysnake.requiem.common.entity.internal.VariableMobilityEntity;
import ladysnake.requiem.common.tag.RequiemEntityTags;
import ladysnake.requiem.mixin.entity.LivingEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Possessable, VariableMobilityEntity {
    private boolean requiem_immovable = RequiemEntityTags.IMMOVABLE.contains(this.getType());

    @Nullable
    @Shadow
    public abstract EntityAttributeInstance getAttributeInstance(EntityAttribute entityAttribute_1);

    @Shadow public abstract AbstractEntityAttributeContainer getAttributeContainer();

    @Shadow public abstract float getAbsorptionAmount();

    @Shadow public float headYaw;
    @Shadow public float field_6283;
    @Shadow public float limbAngle;
    @Shadow public float limbDistance;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public boolean requiem_isImmovable() {
        return requiem_immovable;
    }

    /* * * * * * * * * * *
        Entity overrides
    * * * * * * * * * * */

    @Inject(method = "canMoveVoluntarily", at = @At("HEAD"), cancellable = true)
    private void canMoveVoluntarily(CallbackInfoReturnable<Boolean> cir) {
        if (this.isBeingPossessed()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "initAttributes", at = @At("TAIL"))
    private void initAttributes(CallbackInfo ci) {
        if (this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE) != null) {
            AttributeHelper.substituteAttributeInstance(this.getAttributeContainer(), new CooldownStrengthAttribute((LivingEntity & Possessable)(Object)this));
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        PlayerEntity player = this.getPossessor();
        if (player != null) {
            // Make possessed monsters despawn gracefully
            if (!this.world.isClient) {
                if (this instanceof Monster && this.world.getDifficulty() == Difficulty.PEACEFUL) {
                    player.addChatMessage(new TranslatableTextComponent("requiem.message.peaceful_despawn"), true);
                }
            }
            // Set the player's hit timer for damage animation and stuff
            player.field_6008 = this.field_6008;
            player.setAbsorptionAmount(this.getAbsorptionAmount());
        }
    }

    @Inject(method = "updateState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;travel(Lnet/minecraft/util/math/Vec3d;)V", shift = AFTER))
    private void afterTravel(CallbackInfo ci) {
        PlayerEntity player = this.getPossessor();
        // If anyone has a better idea for immovable mobs, tell me
        if (player != null) {
            this.setRotation(player.yaw, player.pitch);
            this.headYaw = this.field_6283 = this.prevYaw = this.yaw;
            if (!this.requiem_immovable) {
                this.setSwimming(player.isSwimming());
                // Prevent this entity from taking fall damage unless triggered by the possessor
                this.fallDistance = 0;

                this.setPosition(player.x, player.y, player.z);
                this.setVelocity(player.getVelocity());
                // update limb movement
                this.limbAngle = player.limbAngle;
                this.limbDistance = player.limbDistance;
            }
        }
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void baseTick(CallbackInfo ci) {
        this.getMobAbilityController().updateAbilities();
    }

    @SuppressWarnings("InvalidMemberReference")
    @Inject(method = {"pushAwayFrom", "pushAway"}, at = @At("HEAD"), cancellable = true)
    private void pushAwayFrom(Entity entity, CallbackInfo ci) {
        // Prevent infinite propulsion through self collision
        if (entity == this.getPossessor()) {
            ci.cancel();
        }
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeath(DamageSource deathCause, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            ((RequiemPlayer)possessor).getPossessionComponent().stopPossessing();
        }
    }

    @Inject(method = "scheduleVelocityUpdate", at = @At("RETURN"))
    private void scheduleVelocityUpdate(CallbackInfo ci) {
        PlayerEntity player = this.getPossessor();
        if (!world.isClient && this.velocityModified && player != null) {
            player.velocityModified = true;
        }
    }

    @Inject(method = "method_6076", at = @At("HEAD"), cancellable = true)
    private void updateHeldItem(CallbackInfo ci) {
        if (this.isBeingPossessed()) {
            ci.cancel();
        }
    }

    @Inject(method = "method_6020", at = @At("RETURN"))
    private void onStatusEffectAdded(StatusEffectInstance effect, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor instanceof ServerPlayerEntity) {
            possessor.addPotionEffect(new StatusEffectInstance(effect));
        }
    }
    @Inject(method = "method_6009", at = @At("RETURN"))
    private void onStatusEffectUpdated(StatusEffectInstance effect, boolean upgrade, CallbackInfo ci) {
        if (upgrade) {
            PlayerEntity possessor = this.getPossessor();
            if (possessor instanceof ServerPlayerEntity) {
                possessor.addPotionEffect(new StatusEffectInstance(effect));
            }
        }
    }
    @Inject(method = "method_6129", at = @At("RETURN"))
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor instanceof ServerPlayerEntity) {
            possessor.removeStatusEffect(effect.getEffectType());
        }
    }

    /* * * * * * * * * * * *
        Delegation land
     * * * * * * * * * * * */

    /**
     * Knockback
     */
    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void knockback(Entity entity, float vx, double vy, double vz, CallbackInfo ci) {
        PlayerEntity possessing = getPossessor();
        if (possessing != null) {
            possessing.takeKnockback(entity, vx, vy, vz);
            ci.cancel();
        }
    }

    /**
     * Teleport
     * Returns <code>true</code> if the teleportation is successful, otherwise <code>false</code>
     *
     * @param enderTp <code>true</code> for ender particles and sound effect
     */
    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void method_6082(double x, double y, double z, boolean enderTp, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = this.getPossessor();
        if (player != null) {
            cir.setReturnValue(player.teleport(x, y, z, enderTp));
        }
    }

    @Inject(method = "isFallFlying", at = @At("HEAD"), cancellable = true)
    private void isFallFlying(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = this.getPossessor();
        if (player != null) {
            cir.setReturnValue(player.isFallFlying());
        }
    }

    /**
     * Returns Whether this entity is using a shield or equivalent
     */
    @Inject(method = "method_6039", at = @At("HEAD"), cancellable = true)
    private void method_6039(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            cir.setReturnValue(possessor.method_6039());
        }
    }

    @Inject(method = "damageShield", at = @At("HEAD"), cancellable = true)
    private void damageShield(float damage, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null && !this.world.isClient) {
            ((LivingEntityAccessor)possessor).invokeDamageShield(damage);
            this.world.sendEntityStatus(possessor, (byte)29);
            ci.cancel();
        }
    }

    @Inject(method = "getActiveItem", at = @At("HEAD"), cancellable = true)
    private void getActiveItem(CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            cir.setReturnValue(possessor.getActiveItem());
        }
    }

    /**
     * Returns true if this entity's main hand is active
     */
    @Inject(method = "isUsingItem", at = @At("HEAD"), cancellable = true)
    private void isUsingItem(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            cir.setReturnValue(possessor.isUsingItem());
        }
    }
}
