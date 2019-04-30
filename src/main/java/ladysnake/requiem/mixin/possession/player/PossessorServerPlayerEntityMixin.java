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
package ladysnake.requiem.mixin.possession.player;

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.MobResurrectable;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.mixin.entity.EntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Function;

import static ladysnake.requiem.common.network.RequiemNetworking.createCorporealityMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToAllTrackingIncluding;
import static ladysnake.requiem.mixin.server.PlayerTagKeys.*;

@Mixin(ServerPlayerEntity.class)
public abstract class PossessorServerPlayerEntityMixin extends PlayerEntity implements MobResurrectable, RequiemPlayer {
    @Nullable
    private CompoundTag requiem_possessedEntityTag;

    public PossessorServerPlayerEntityMixin(World world_1, GameProfile gameProfile_1) {
        super(world_1, gameProfile_1);
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
                formerPossessed.setPositionAndAngles(this);
                if (world.spawnEntity(formerPossessed)) {
                    this.getPossessionComponent().startPossessing((MobEntity) formerPossessed);
                } else {
                    Requiem.LOGGER.error("Failed to spawn possessed entity {}", formerPossessed);
                }
            } else {
                Requiem.LOGGER.error("Could not recreate possessed entity {}", requiem_possessedEntityTag);
            }
            this.requiem_possessedEntityTag = null;
        }
    }

    private void setResurrectionEntity(@Nullable CompoundTag serializedSecondLife) {
        this.requiem_possessedEntityTag = serializedSecondLife;
    }

    @Inject(method = "changeDimension", at = @At("HEAD"))
    private void changePossessedDimension(DimensionType dim, CallbackInfoReturnable<Entity> info) {
        PossessionComponent possessionComponent = this.getPossessionComponent();
        if (possessionComponent.isPossessing()) {
            MobEntity current = possessionComponent.getPossessedEntity();
            if (current != null && !current.removed) {
                this.setResurrectionEntity(current);
                current.remove();
            }
        }
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void clonePlayer(ServerPlayerEntity original, boolean fromEnd, CallbackInfo ci) {
        this.requiem_possessedEntityTag = ((PossessorServerPlayerEntityMixin) (Object) original).requiem_possessedEntityTag;
        if (this.requiem_possessedEntityTag != null) {
            this.inventory.clone(original.inventory);
        }
    }

    @Inject(method = "onTeleportationDone", at = @At("HEAD"))
    private void onTeleportDone(CallbackInfo info) {
        sendToAllTrackingIncluding(this, createCorporealityMessage(this));
        spawnResurrectionEntity();
    }

    @Inject(method = "fall", at = @At("HEAD"), cancellable = true)
    private void onFall(double fallY, boolean onGround, BlockState floorBlock, BlockPos floorPos, CallbackInfo info) {
        Entity possessed = this.getPossessionComponent().getPossessedEntity();
        if (possessed != null) {
            possessed.fallDistance = this.fallDistance;
            ((EntityAccessor) possessed).onFall(fallY, onGround, floorBlock, floorPos);
        }
    }

    @Inject(method = "method_6020", at = @At("RETURN"))
    private void onStatusEffectAdded(StatusEffectInstance effect, CallbackInfo ci) {
        MobEntity possessed = this.getPossessionComponent().getPossessedEntity();
        if (possessed != null) {
            possessed.addPotionEffect(new StatusEffectInstance(effect));
        }
    }
    @Inject(method = "method_6009", at = @At("RETURN"))
    private void onStatusEffectUpdated(StatusEffectInstance effect, boolean upgrade, CallbackInfo ci) {
        if (upgrade) {
            MobEntity possessed = this.getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                possessed.addPotionEffect(new StatusEffectInstance(effect));
            }
        }
    }
    @Inject(method = "method_6129", at = @At("RETURN"))
    private void onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci) {
        MobEntity possessed = this.getPossessionComponent().getPossessedEntity();
        if (possessed != null) {
            possessed.removeStatusEffect(effect.getEffectType());
        }
    }


    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writePossessedMobToTag(CompoundTag tag, CallbackInfo info) {
        Entity possessedEntity = this.getPossessionComponent().getPossessedEntity();
        if (possessedEntity != null) {
            Entity possessedEntityVehicle = possessedEntity.getTopmostVehicle();
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
}
