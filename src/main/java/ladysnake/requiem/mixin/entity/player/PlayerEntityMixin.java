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
package ladysnake.requiem.mixin.entity.player;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.entity.internal.VariableMobilityEntity;
import ladysnake.requiem.common.impl.movement.PlayerMovementAlterer;
import ladysnake.requiem.common.impl.possession.PossessionComponentImpl;
import ladysnake.requiem.common.impl.remnant.NullRemnantState;
import ladysnake.requiem.common.remnant.RemnantStates;
import ladysnake.requiem.common.tag.RequiemEntityTags;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ladysnake.requiem.common.network.RequiemNetworking.createCorporealityMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToAllTrackingIncluding;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements RequiemPlayer {

    /* Implementation of RequiemPlayer */

    @Shadow @Final public PlayerInventory inventory;
    private static final String TAG_REMNANT_DATA = "requiem:remnant_data";

    private RemnantState remnantState = NullRemnantState.NULL_STATE;
    private final PossessionComponent possessionComponent = new PossessionComponentImpl((PlayerEntity) (Object) this);
    private final MovementAlterer movementAlterer = new PlayerMovementAlterer((PlayerEntity)(Object)this);

    @Override
    public RemnantState getRemnantState() {
        return this.remnantState;
    }

    @Override
    public PossessionComponent getPossessionComponent() {
        return possessionComponent;
    }

    @Override
    public MovementAlterer getMovementAlterer() {
        return this.movementAlterer;
    }

    @Override
    public void setRemnant(boolean remnant) {
        if (remnant != this.isRemnant()) {
            RemnantState state = remnant ? RemnantStates.LARVA.create((PlayerEntity) (Object) this) : NullRemnantState.NULL_STATE;
            this.setRemnantState(state);
        }
    }

    @Override
    public boolean isRemnant() {
        return !(this.remnantState instanceof NullRemnantState);
    }

    private void setRemnantState(RemnantState handler) {
        this.remnantState.setSoul(false);
        this.remnantState = handler;
        if (!this.world.isClient) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            sendToAllTrackingIncluding(player, createCorporealityMessage(player));
        }
    }

    @Inject(method = "updateMovement", at = @At("HEAD"))
    private void updateMovementAlterer(CallbackInfo info) {
        this.movementAlterer.update();
        this.remnantState.update();
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void travel(CallbackInfo info) {
        Entity possessed = (Entity) this.getPossessionComponent().getPossessedEntity();
        if (possessed != null && ((VariableMobilityEntity)possessed).requiem_isImmovable()) {
            info.cancel();
        }
    }

    /* NBT (de)serialization of added fields */

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        CompoundTag remnantData = new CompoundTag();
        remnantData.putString("id", RequiemRegistries.REMNANT_STATES.getId(this.getRemnantState().getType()).toString());
        tag.put(TAG_REMNANT_DATA, this.remnantState.toTag(remnantData));
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        CompoundTag remnantTag = tag.getCompound(TAG_REMNANT_DATA);
        RemnantType remnantType = RequiemRegistries.REMNANT_STATES.get(new Identifier(remnantTag.getString("id")));
        RemnantState handler = remnantType.create((PlayerEntity) (Object) this);
        handler.fromTag(remnantTag);
        this.setRemnantState(handler);
    }

    /* Actual modifications of vanilla behaviour */

    /**
     * Players' base movement speed is reset each tick to their walking speed.
     * We don't want that when a possession is occurring.
     *
     * @param attr the {@code this} attribute reference
     * @param value the value that is supposed to be assigned
     */
    @Redirect(method = "updateMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeInstance;setBaseValue(D)V"))
    private void ignoreSpeedResetDuringPossession(EntityAttributeInstance attr, double value) {
        if (!this.getPossessionComponent().isPossessing()) {
            attr.setBaseValue(value);
        }
    }

    /**
     * Players' sizes are hardcoded in an immutable enum map.
     * This injection delegates the call to the possessed entity, if any.
     */
    @Inject(method = "getSize", at = @At("HEAD"), cancellable = true)
    private void adjustSize(EntityPose pose, CallbackInfoReturnable<EntitySize> cir) {
        Entity possessedEntity = (Entity) this.getPossessionComponent().getPossessedEntity();
        if (possessedEntity != null) {
            cir.setReturnValue(possessedEntity.getSize(pose));
        }
    }

    protected PlayerEntityMixin(EntityType<? extends PlayerEntity> type, World world) {
        super(type, world);
    }

}
