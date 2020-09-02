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
package ladysnake.requiem.mixin.common.entity.player;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.entity.internal.VariableMobilityEntity;
import ladysnake.requiem.common.impl.remnant.dialogue.PlayerDialogueTracker;
import ladysnake.requiem.common.tag.RequiemItemTags;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements RequiemPlayer {

    /* Implementation of RequiemPlayer */

    @Shadow @Final public PlayerAbilities abilities;
    @Shadow
    protected HungerManager hungerManager;
    private static final EntityDimensions REQUIEM$SOUL_SNEAKING_SIZE = EntityDimensions.changing(0.6f, 0.6f);

    private final DialogueTracker dialogueTracker = new PlayerDialogueTracker((PlayerEntity) (Object) this);

    @Override
    public DialogueTracker getDialogueTracker() {
        return this.dialogueTracker;
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void updateMovementAlterer(CallbackInfo info) {
        MovementAlterer.KEY.get(this).update();
        RemnantComponent.KEY.get(this).update();
        PossessionComponent.KEY.get(this).update();
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void travel(CallbackInfo info) {
        Entity possessed = PossessionComponent.getPossessedEntity(this);
        if (possessed != null && ((VariableMobilityEntity) possessed).requiem_isImmovable()) {
            if (!world.isClient && (this.getX() != possessed.getX() || this.getY() != possessed.getY() || this.getZ() != possessed.getZ())) {
                ServerPlayNetworkHandler networkHandler = ((ServerPlayerEntity) (Object) this).networkHandler;
                networkHandler.teleportRequest(possessed.getX(), possessed.getY(), possessed.getZ(), this.yaw, this.pitch, EnumSet.allOf(PlayerPositionLookS2CPacket.Flag.class));
                networkHandler.syncWithPlayerPosition();
            }
            info.cancel();
        }
    }

    /* Actual modifications of vanilla behaviour */

    @Inject(method = "eatFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;eat(Lnet/minecraft/item/Item;Lnet/minecraft/item/ItemStack;)V"))
    private void eatZombieFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        MobEntity possessedEntity = PossessionComponent.KEY.get(this).getPossessedEntity();
        if (possessedEntity instanceof ZombieEntity && stack.getItem().isFood()) {
            if (RequiemItemTags.RAW_MEATS.contains(stack.getItem()) || RequiemItemTags.RAW_FISHES.contains(stack.getItem()) && possessedEntity instanceof DrownedEntity) {
                FoodComponent food = stack.getItem().getFoodComponent();
                assert food != null;
                possessedEntity.heal(food.getHunger());
            }
        }
        if (possessedEntity != null && possessedEntity.isUndead() && RequiemItemTags.UNDEAD_CURES.contains(stack.getItem()) && possessedEntity.hasStatusEffect(StatusEffects.WEAKNESS)) {
            PossessionComponent.KEY.get(this).startCuring();
        }
    }

    @Inject(method = "isSwimming", at = @At("HEAD"), cancellable = true)
    private void flyLikeSuperman(CallbackInfoReturnable<Boolean> cir) {
        if (this.abilities.flying && this.isSprinting() && RemnantComponent.KEY.get(this).isIncorporeal()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private void flySwimVertically(Vec3d motion, CallbackInfo ci) {
        double yMotion = this.getRotationVector().y;
        double modifier = yMotion < -0.2D ? 0.085D : 0.06D;
        // If the motion change would not be applied, apply it ourselves
        if (yMotion > 0.0D && !this.jumping && this.world.getBlockState(new BlockPos(this.getX(), this.getY() + 1.0D - 0.1D, this.getZ())).getFluidState().isEmpty() && RemnantComponent.KEY.get(this).isIncorporeal()) {
            Vec3d velocity = this.getVelocity();
            this.setVelocity(velocity.add(0.0D, (yMotion - velocity.y) * modifier, 0.0D));
        }
    }

    /**
     * Players' sizes are hardcoded in an immutable enum map.
     * This injection delegates the call to the possessed entity, if any.
     */
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void adjustSize(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (RemnantComponent.KEY.get(this).isSoul()) {
            Entity possessedEntity = PossessionComponent.KEY.get(this).getPossessedEntity();
            if (possessedEntity != null) {
                cir.setReturnValue(possessedEntity.getDimensions(pose));
            } else if (pose == EntityPose.CROUCHING) {
                cir.setReturnValue(REQUIEM$SOUL_SNEAKING_SIZE);
            }
        }
    }

    // 1.27 is the sneaking eye height
    @Inject(method = "getActiveEyeHeight", at = {@At(value = "CONSTANT", args = "floatValue=1.27")}, cancellable = true)
    private void adjustSoulSneakingEyeHeight(EntityPose pose, EntityDimensions size, CallbackInfoReturnable<Float> cir) {
        if (RemnantComponent.KEY.get(this).isIncorporeal()) {
            cir.setReturnValue(0.4f);
        }
    }

    @Inject(method = "canConsume", at = @At("RETURN"), cancellable = true)
    private void canSoulConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        if (RemnantComponent.KEY.get(this).isSoul()) {
            Possessable possessed = (Possessable) PossessionComponent.KEY.get(this).getPossessedEntity();
            cir.setReturnValue(ignoreHunger || possessed != null && possessed.isRegularEater() && this.hungerManager.isNotFull());
        }
    }

    @Inject(method = "canFoodHeal", at = @At("RETURN"), cancellable = true)
    private void canFoodHealPossessed(CallbackInfoReturnable<Boolean> cir) {
        if (RemnantComponent.KEY.get(this).isSoul()) {
            LivingEntity possessed = PossessionComponent.KEY.get(this).getPossessedEntity();
            cir.setReturnValue(possessed != null && ((Possessable) possessed).isRegularEater() && possessed.getHealth() > 0 && possessed.getHealth() < possessed.getMaxHealth());
        }
    }

    @Inject(method = "addExhaustion", slice = @Slice(to = @At("INVOKE:FIRST")), at = @At(value = "RETURN"))
    private void addExhaustion(float exhaustion, CallbackInfo ci) {
        Possessable possessed = (Possessable) PossessionComponent.KEY.get(this).getPossessedEntity();
        if (possessed != null && possessed.isRegularEater()) {
            if (!this.world.isClient) {
                this.hungerManager.addExhaustion(exhaustion);
            }
        }
    }

    protected PlayerEntityMixin(EntityType<? extends PlayerEntity> type, World world) {
        super(type, world);
    }

}
