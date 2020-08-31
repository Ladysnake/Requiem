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
package ladysnake.requiem.mixin.common.possession.player;

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.MobResurrectable;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Function;

import static ladysnake.requiem.mixin.common.server.PlayerTagKeys.*;

@Mixin(ServerPlayerEntity.class)
public abstract class PossessorServerPlayerEntityMixin extends PlayerEntity implements MobResurrectable, RequiemPlayer {
    @Nullable
    private CompoundTag requiem_possessedEntityTag;

    public PossessorServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void setResurrectionEntity(MobEntity secondLife) {
        CompoundTag tag = new CompoundTag();
        if (secondLife.saveSelfToTag(tag)) {
            setResurrectionEntity(tag);
        } else {
            Requiem.LOGGER.warn("Could not serialize possessed entity {} !", secondLife);
        }
    }

    @Override
    public void spawnResurrectionEntity() {
        if (this.requiem_possessedEntityTag != null) {
            Entity formerPossessed = EntityType.loadEntityWithPassengers(
                    this.requiem_possessedEntityTag,
                    world,
                    Function.identity()
            );
            if (formerPossessed instanceof MobEntity) {
                formerPossessed.copyPositionAndRotation(this);
                if (world.spawnEntity(formerPossessed)) {
                    if (this.asPossessor().startPossessing((MobEntity) formerPossessed)) {
                        RequiemCriteria.PLAYER_RESURRECTED_AS_ENTITY.handle((ServerPlayerEntity)(Object) this, formerPossessed);
                    }
                } else {
                    Requiem.LOGGER.error("Failed to spawn possessed entity {}", formerPossessed);
                }
            } else {
                Requiem.LOGGER.error("Could not recreate possessed entity {}", requiem_possessedEntityTag);
            }
            this.requiem_possessedEntityTag = null;
        }
    }

    @Unique
    private void setResurrectionEntity(@Nullable CompoundTag serializedSecondLife) {
        this.requiem_possessedEntityTag = serializedSecondLife;
    }

    @Inject(method = "moveToWorld", at = @At(value = "HEAD", shift = At.Shift.AFTER))   // Let cancelling mixins do their job
    private void changePossessedDimension(ServerWorld dim, CallbackInfoReturnable<Entity> info) {
        prepareDimensionChange();
    }

    @Inject(method = "teleport", at = @At(value = "HEAD", shift = At.Shift.AFTER))   // Let cancelling mixins do their job
    private void changePossessedDimension(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        prepareDimensionChange();
    }

    @Unique
    private void prepareDimensionChange() {
        PossessionComponent possessionComponent = this.asPossessor();
        if (possessionComponent.isPossessing()) {
            MobEntity current = possessionComponent.getPossessedEntity();
            if (current != null && !current.removed) {
                this.setResurrectionEntity(current);
                current.remove();
            }
        }
    }

    @Inject(method = "moveToWorld", at = @At(value = "RETURN", ordinal = 1))
    private void onTeleportDone(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        spawnResurrectionEntity();
    }

    @Inject(method = "teleport", at = @At(value = "RETURN"))
    private void onTeleportDone(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        spawnResurrectionEntity();
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void clonePlayer(ServerPlayerEntity original, boolean fromEnd, CallbackInfo ci) {
        // We can safely cast a class to a mixin from another mixin
        //noinspection ConstantConditions
        this.requiem_possessedEntityTag = ((PossessorServerPlayerEntityMixin) (Object) original).requiem_possessedEntityTag;
        if (this.requiem_possessedEntityTag != null) {
            this.inventory.clone(original.inventory);
        }
    }

    @Inject(method = "swingHand", at = @At("HEAD"))
    private void swingHand(Hand hand, CallbackInfo ci) {
        LivingEntity possessed = this.asPossessor().getPossessedEntity();
        if (possessed != null) {
            possessed.swingHand(hand);
        }
    }

    @Inject(method = "onStatusEffectApplied", at = @At("RETURN"))
    private void onStatusEffectAdded(StatusEffectInstance effect, CallbackInfo ci) {
        MobEntity possessed = this.asPossessor().getPossessedEntity();
        if (possessed != null) {
            possessed.addStatusEffect(new StatusEffectInstance(effect));
        }
    }
    @Inject(method = "onStatusEffectUpgraded", at = @At("RETURN"))
    private void onStatusEffectUpdated(StatusEffectInstance effect, boolean upgrade, CallbackInfo ci) {
        if (upgrade) {
            MobEntity possessed = this.asPossessor().getPossessedEntity();
            if (possessed != null) {
                possessed.addStatusEffect(new StatusEffectInstance(effect));
            }
        }
    }
    @Inject(method = "onStatusEffectRemoved", at = @At("RETURN"))
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        MobEntity possessed = this.asPossessor().getPossessedEntity();
        if (possessed != null) {
            possessed.removeStatusEffect(effect.getEffectType());
        }
    }


    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writePossessedMobToTag(CompoundTag tag, CallbackInfo info) {
        Entity possessedEntity = this.asPossessor().getPossessedEntity();
        if (possessedEntity != null) {
            Entity possessedEntityVehicle = possessedEntity.getRootVehicle();
            CompoundTag possessedRoot = new CompoundTag();
            CompoundTag serializedPossessed = new CompoundTag();
            possessedEntityVehicle.saveToTag(serializedPossessed);
            possessedRoot.put(POSSESSED_ENTITY_TAG, serializedPossessed);
            possessedRoot.putUuid(POSSESSED_UUID_TAG, possessedEntity.getUuid());
            tag.put(POSSESSED_ROOT_TAG, possessedRoot);
        } else if (this.requiem_possessedEntityTag != null) {
            CompoundTag possessedRoot = new CompoundTag();
            possessedRoot.put(POSSESSED_ENTITY_TAG, this.requiem_possessedEntityTag);
            possessedRoot.putUuid(POSSESSED_UUID_TAG, this.requiem_possessedEntityTag.getUuid("UUID"));
            tag.put(POSSESSED_ROOT_TAG, possessedRoot);
        }
    }

    /**
     * Return a {@code PlayerEntity} instance that corresponds to this player.
     * Calling {@link #from(PlayerEntity)} on the returned value returns {@code this} instance.
     *
     * @return {@code this} as a {@link PlayerEntity}
     * @since 1.0.0
     */
    @Contract(pure = true)
    public abstract PlayerEntity asPlayer();
}
