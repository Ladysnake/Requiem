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
package ladysnake.requiem.mixin.possession.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.impl.ability.ImmutableMobAbilityController;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MobEntity.class)
public abstract class PossessableMobEntityMixin extends LivingEntity implements Possessable {

    @Shadow
    public abstract void setAttacking(boolean boolean_1);

    @Shadow
    public abstract boolean isAttacking();

    @Unique
    private MobAbilityController abilityController = MobAbilityController.DUMMY;
    @Unique
    private int attackingCountdown;

    public PossessableMobEntityMixin(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initAbilities(CallbackInfo ci) {
        if (world != null && !world.isClient) {
            this.abilityController = new ImmutableMobAbilityController<>(MobAbilityRegistry.instance().getConfig((MobEntity)(Object)this), (MobEntity & Possessable)(Object)this);
        }
    }

    @Override
    public MobAbilityController getMobAbilityController() {
        return abilityController;
    }

    @Inject(method = "setAttacking", at = @At("RETURN"))
    private void resetAttackMode(boolean attacking, CallbackInfo ci) {
        if (attacking && this.isBeingPossessed()) {
            attackingCountdown = 100;
        }
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void resetAttackMode(CallbackInfo ci) {
        if (this.isAttacking() && !this.isUsingItem() && this.isBeingPossessed()) {
            this.attackingCountdown--;
            if (this.attackingCountdown == 0) {
                this.setAttacking(false);
            }
        }
    }

    @Inject(method = "getEquippedStack", at = @At("HEAD"), cancellable = true)
    private void getEquippedStack(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            cir.setReturnValue(possessor.getEquippedStack(slot));
        }
    }

    @Inject(method = "equipStack", at = @At("HEAD"), cancellable = true)
    private void setEquippedStack(EquipmentSlot slot, ItemStack item, CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null && !world.isClient) {
            possessor.equipStack(slot, item);
        }
    }

    @Inject(method = "method_29243", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EquipmentSlot;values()[Lnet/minecraft/entity/EquipmentSlot;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private <T extends MobEntity> void possessConvertedZombie(EntityType<T> type, CallbackInfoReturnable<T> ci, T converted) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            PossessionComponent possessionComponent = ((RequiemPlayer)possessor).asPossessor();
            possessionComponent.stopPossessing(false);
            // The possession will start when the entity is added to the world
            ((Possessable)converted).setPossessor(possessor);
        }
    }
}
