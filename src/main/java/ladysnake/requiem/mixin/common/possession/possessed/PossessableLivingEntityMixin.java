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
package ladysnake.requiem.mixin.common.possession.possessed;

import com.google.common.base.Preconditions;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.VanillaRequiemPlugin;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.entity.ai.DisableableBrain;
import ladysnake.requiem.common.entity.attribute.CooldownStrengthAttribute;
import ladysnake.requiem.common.entity.attribute.DelegatingAttribute;
import ladysnake.requiem.common.entity.effect.AttritionStatusEffect;
import ladysnake.requiem.common.entity.internal.VariableMobilityEntity;
import ladysnake.requiem.common.gamerule.RequiemGamerules;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.mixin.common.access.LivingEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

/**
 * Implementation of {@link Possessable} on living entities
 */
@Mixin(LivingEntity.class)
abstract class PossessableLivingEntityMixin extends Entity implements Possessable, VariableMobilityEntity {
    @Unique
    private final boolean requiem_immovable = RequiemEntityTypeTags.IMMOVABLE.contains(this.getType());
    @Unique
    private final boolean requiem_regularEater = RequiemEntityTypeTags.EATERS.contains(this.getType());
    @Unique
    private boolean requiem_wasCustomNameVisible;
    @Unique
    @Nullable
    private Text requiem_previousCustomName;
    @Unique
    @Nullable
    private UUID requiem_previousPossessorUuid;

    @Shadow
    public abstract EntityAttributeInstance getAttributeInstance(EntityAttribute entityAttribute_1);

    @Shadow public abstract float getHealth();

    @Shadow public abstract float getAbsorptionAmount();

    @Shadow public float headYaw;
    @Shadow public float limbAngle;
    @Shadow public float limbDistance;

    @Shadow
    public float bodyYaw;

    @Shadow
    @Nullable
    public abstract LivingEntity getAttacker();

    @Shadow
    @Final
    private AttributeContainer attributes;

    @Shadow
    public abstract boolean isUsingItem();

    @Shadow
    public abstract Brain<?> getBrain();

    @Nullable
    private PlayerEntity possessor;

    public PossessableLivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /* * * * * * * * * * * * * * * *
        Interface implementations
    * * * * * * * * * * * * * * * */

    @Override
    public Optional<UUID> getPossessorUuid() {
        return Optional.ofNullable(this.possessor).map(PlayerEntity::getUuid);
    }

    @Override
    public boolean isBeingPossessed() {
        return this.possessor != null;
    }

    @Nullable
    @Override
    public PlayerEntity getPossessor() {
        if (this.possessor != null && this.possessor.removed) {
            PossessionComponent.get(this.possessor).stopPossessing();
            // Make doubly sure
            this.setPossessor(null);
        }
        return possessor;
    }

    @Override
    public boolean canBePossessedBy(PlayerEntity player) {
        return !this.removed && this.getHealth() > 0 && (!this.isBeingPossessed() || this.possessor == player);
    }

    @Override
    public void setPossessor(@CheckForNull PlayerEntity possessor) {
        if (possessor == this.possessor) {
            return;
        }
        // we need a cast here to trick the compiler
        // clever Idea assumes possessedEntity cannot be this because of the wrong class, which is wrong because Mixin
        //noinspection ConstantConditions
        if ((this.possessor != null && PossessionComponent.get(this.possessor).getPossessedEntity() == (Entity) this) && !this.world.isClient) {
            throw new IllegalStateException("Players must stop possessing an entity before it can change possessor!");
        }

        if (possessor == null) {
            assert this.possessor != null;
            this.requiem_previousPossessorUuid = this.possessor.getUuid();
        }

        this.possessor = possessor;

        ((DisableableBrain) this.getBrain()).requiem_setDisabled(this.possessor != null);

        this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(VanillaRequiemPlugin.INHERENT_MOB_SLOWNESS_UUID);
        if (possessor != null) {
            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).addTemporaryModifier(VanillaRequiemPlugin.INHERENT_MOB_SLOWNESS);
        }

        updateName(possessor);
    }

    @Unique
    private void updateName(@CheckForNull PlayerEntity possessor) {
        if (possessor != null) {
            if (this.requiem_previousCustomName == null) {
                this.requiem_wasCustomNameVisible = this.isCustomNameVisible();
                this.requiem_previousCustomName = this.getCustomName();
            }
            this.refreshPossessorNameTag();
        } else {
            this.setCustomNameVisible(this.requiem_wasCustomNameVisible);
            this.setCustomName(this.requiem_previousCustomName);
            this.requiem_wasCustomNameVisible = false;
            this.requiem_previousCustomName = null;
        }
    }

    public void refreshPossessorNameTag() {
        Preconditions.checkState(this.possessor != null);

        if (world.getGameRules().getBoolean(RequiemGamerules.SHOW_POSSESSOR_NAMETAG)) {
            this.setCustomName(this.possessor.getName());
            this.setCustomNameVisible(true);
        } else {
            this.setCustomName(this.requiem_previousCustomName);
            this.setCustomNameVisible(this.requiem_wasCustomNameVisible);
        }
    }

    @Override
    public boolean requiem_isImmovable() {
        return requiem_immovable;
    }

    @Override
    public boolean isRegularEater() {
        return requiem_regularEater;
    }

    /* * * * * * * * * * *
        Entity overrides
    * * * * * * * * * * */

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        if (this.isBeingPossessed()) {
            Text text = this.requiem_previousCustomName;
            if (text != null) {
                tag.putString("CustomName", Text.Serializer.toJson(text));
            } else {
                tag.remove("CustomName");
            }

            tag.putBoolean("CustomNameVisible", this.requiem_wasCustomNameVisible);
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;canMoveVoluntarily()Z", ordinal = 1))
    private void requiem_mobTick(CallbackInfo ci) {
        if (this.isBeingPossessed() && !world.isClient) {
            this.requiem_mobTick();
        }
    }

    protected void requiem_mobTick() {
        // NO-OP
    }

    @Inject(method = "canMoveVoluntarily", at = @At("HEAD"), cancellable = true)
    private void canMoveVoluntarily(CallbackInfoReturnable<Boolean> cir) {
        if (this.isBeingPossessed()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAttributes(CallbackInfo ci) {
        // Entities may or may not register ATTACK_DAMAGE
        //noinspection ConstantConditions
        if (this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
            DelegatingAttribute.replaceAttribute(attributes, new CooldownStrengthAttribute((LivingEntity & Possessable)(Object) this));
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        PlayerEntity player = this.getPossessor();
        if (player != null) {
            // Make possessed monsters despawn gracefully
            if (!this.world.isClient) {
                if (this instanceof Monster && this.world.getDifficulty() == Difficulty.PEACEFUL) {
                    player.sendMessage(new TranslatableText("requiem.message.peaceful_despawn"), true);
                }
            }
            // Set the player's hit timer for damage animation and stuff
            player.timeUntilRegen = this.timeUntilRegen;
            player.setAbsorptionAmount(this.getAbsorptionAmount());
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;travel(Lnet/minecraft/util/math/Vec3d;)V", shift = AFTER))
    private void afterTravel(CallbackInfo ci) {
        PlayerEntity player = this.getPossessor();
        // If anyone has a better idea for immovable mobs, tell me
        if (player != null) {
            this.setRotation(player.yaw, player.pitch);
            this.headYaw = this.bodyYaw = this.prevYaw = this.yaw;
            if (!this.requiem_immovable) {
                this.setSwimming(player.isSwimming());
                // Prevent this entity from taking fall damage unless triggered by the possessor
                this.fallDistance = 0;

                this.setVelocity(player.getVelocity());
                this.move(MovementType.SELF, this.getVelocity());
                this.updatePosition(player.getX(), player.getY(), player.getZ());
                // update limb movement
                this.limbAngle = player.limbAngle;
                this.limbDistance = player.limbDistance;
            }
        }
    }

    @Inject(method = {"pushAwayFrom", "pushAway"}, at = @At("HEAD"), cancellable = true)
    private void pushAwayFrom(Entity entity, CallbackInfo ci) {
        // Prevent infinite propulsion through self collision
        if (entity == this.getPossessor()) {
            ci.cancel();
        }
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;drop(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void onDeath(DamageSource deathCause, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            PossessionComponent.get(possessor).stopPossessing();
            possessor.setAttacker(this.getAttacker());
            AttritionStatusEffect.apply(possessor);
        } else if (this.requiem_previousPossessorUuid != null) {
            PlayerEntity previousPossessor = this.world.getPlayerByUuid(this.requiem_previousPossessorUuid);

            if (previousPossessor != null) {
                RequiemCriteria.DEATH_AFTER_POSSESSION.handle((ServerPlayerEntity) previousPossessor, this, deathCause);
            }
        }
    }

    @Inject(method = "scheduleVelocityUpdate", at = @At("RETURN"))
    private void scheduleVelocityUpdate(CallbackInfo ci) {
        PlayerEntity player = this.getPossessor();
        if (!world.isClient && this.velocityModified && player != null) {
            player.velocityModified = true;
        }
    }

    @Inject(method = "tickActiveItemStack", at = @At("HEAD"), cancellable = true)
    private void updateHeldItem(CallbackInfo ci) {
        if (this.isBeingPossessed()) {
            ci.cancel();
        }
    }

    @Inject(method = "onStatusEffectApplied", at = @At("RETURN"))
    private void onStatusEffectAdded(StatusEffectInstance effect, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor instanceof ServerPlayerEntity) {
            possessor.addStatusEffect(new StatusEffectInstance(effect));
        }
    }
    @Inject(method = "onStatusEffectUpgraded", at = @At("RETURN"))
    private void onStatusEffectUpdated(StatusEffectInstance effect, boolean upgrade, CallbackInfo ci) {
        if (upgrade) {
            PlayerEntity possessor = this.getPossessor();
            if (possessor instanceof ServerPlayerEntity) {
                possessor.addStatusEffect(new StatusEffectInstance(effect));
            }
        }
    }
    @Inject(method = "onStatusEffectRemoved", at = @At("RETURN"))
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor instanceof ServerPlayerEntity) {
            possessor.removeStatusEffect(effect.getEffectType());
        }
    }

    /*
    clearPotionEffects calls onStatusEffectRemoved before actually removing it, unlike everything else.
    This causes issues, to which one of the simplest fix is to call remove() right after calling next().
     */

    @Redirect(method = "clearStatusEffects", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", remap = false))
    private Object swapIteratorOperationsPart1(Iterator<?> iterator) {
        Object next = iterator.next();
        iterator.remove();
        return next;
    }
    @Redirect(method = "clearStatusEffects", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", remap = false))
    private void swapIteratorOperationsPart2(Iterator<?> iterator) {
        // NO-OP
    }

    /* * * * * * * * * * * *
        Delegation land
     * * * * * * * * * * * */

    /**
     * Knockback
     */
    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void knockback(float vx, double vy, double vz, CallbackInfo ci) {
        PlayerEntity possessing = getPossessor();
        if (possessing != null) {
            possessing.takeKnockback(vx, vy, vz);
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
    private void teleportPossessor(double x, double y, double z, boolean enderTp, CallbackInfoReturnable<Boolean> cir) {
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
    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void isBlocking(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            cir.setReturnValue(possessor.isBlocking());
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
