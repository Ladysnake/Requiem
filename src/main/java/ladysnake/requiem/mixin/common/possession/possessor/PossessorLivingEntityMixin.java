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
package ladysnake.requiem.mixin.common.possession.possessor;

import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.util.DamageHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class PossessorLivingEntityMixin extends PossessorEntityMixin {

    @Shadow
    @Nullable
    private LivingEntity attacker;

    @ModifyVariable(
        method = "travel",
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/enchantment/EnchantmentHelper;getDepthStrider(Lnet/minecraft/entity/LivingEntity;)I"
            )
        ),
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
            ordinal = 0
        ),
        ordinal = 0
    )
    private float fixUnderwaterVelocity(float /* float_4 */ speedAmount) {
        MovementAlterer alterer = MovementAlterer.KEY.getNullable(this);
        if (alterer != null) {
            return alterer.getSwimmingAcceleration(speedAmount);
        }
        return speedAmount;
    }

    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    protected void requiem$canClimb(CallbackInfoReturnable<Boolean> cir) {
        // overridden by PossessorPlayerEntityMixin
    }

    @Inject(method = "collides", at = @At("RETURN"), cancellable = true)
    protected void requiem$preventSoulsCollision(CallbackInfoReturnable<Boolean> info) {
        // overridden by PossessorPlayerEntityMixin
    }

    @Inject(
        method = "fall",
        at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/LivingEntity;fallDistance:F", ordinal = 0),
        cancellable = true
    )
    private void onFall(double fallY, boolean onGround, BlockState floorBlock, BlockPos floorPos, CallbackInfo info) {
        if (world.isClient) return;

        Entity possessed = PossessionComponent.getPossessedEntity((Entity) (Object) this);
        if (possessed != null) {
            possessed.fallDistance = this.fallDistance;
            possessed.copyPositionAndRotation((Entity) (Object) this);
            possessed.move(MovementType.SELF, Vec3d.ZERO);
            // We know that possessed is a LivingEntity, Mixin will translate to that type automatically
            //noinspection ConstantConditions
            ((PossessorLivingEntityMixin) (Object) possessed).fall(fallY, onGround, floorBlock, floorPos);
        }
    }

    /**
     * Marks possessed entities as the attacker for any damage caused by their possessor.
     *
     * @param source damage dealt
     * @param amount amount of damage dealt
     */
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private DamageSource proxyDamage(DamageSource source, DamageSource s, float amount) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof LivingEntity) {
            DamageSource newSource = DamageHelper.tryProxyDamage(source, (LivingEntity) attacker);
            if (newSource != null) {
                return newSource;
            }
        }
        return source;
    }

    @Inject(method = "damage",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/PlayerHurtEntityCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;FFZ)V")),
        at = @At(value = "JUMP", opcode = Opcodes.IFEQ),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        allow = 1
    )
    private void triggerCriterion(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, float dealt, boolean blocked, float attackAngle, boolean playDamageEffects, @Nullable Entity attacker, boolean didDamage) {
        if (attacker != null) {
            PlayerEntity possessor = ((ProtoPossessable) attacker).getPossessor();
            if (possessor instanceof ServerPlayerEntity) {
                RequiemCriteria.POSSESSED_HIT_ENTITY.handle(((ServerPlayerEntity) possessor), attacker, (Entity) (Object) this, source, dealt, amount, blocked);
            }
        }
    }
}
