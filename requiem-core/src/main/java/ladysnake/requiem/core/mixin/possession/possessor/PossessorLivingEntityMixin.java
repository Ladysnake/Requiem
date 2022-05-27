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
package ladysnake.requiem.core.mixin.possession.possessor;

import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.core.tag.RequiemCoreTags;
import ladysnake.requiem.core.util.DamageHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PossessorLivingEntityMixin extends PossessorEntityMixin {

    @Shadow
    public abstract void setSprinting(boolean sprinting);

    @Invoker("getAttributeInstance")
    public abstract @Nullable EntityAttributeInstance requiem$getAttributeInstance(EntityAttribute attribute);

    @ModifyArg(method = "swimUpward", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"), index = 1)
    private double updateSwimVelocity(double upwardsVelocity) {
        MovementAlterer alterer = MovementAlterer.KEY.getNullable(this);
        if (alterer != null) {
            return alterer.getSwimmingUpwardsVelocity(upwardsVelocity);
        }
        return upwardsVelocity;
    }

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
    @Inject(method = "isFallFlying", at = @At("RETURN"), cancellable = true)
    protected void requiem$canFly(CallbackInfoReturnable<Boolean> cir) {
        //Overidden
    }

    @Inject(method = "setSprinting", at = @At("RETURN"))
    protected void requiem$setSprinting(boolean sprinting, CallbackInfo ci) {
        // overridden by PossessorPlayerEntityMixin
    }

    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    protected void requiem$canClimb(CallbackInfoReturnable<Boolean> cir) {
        // overridden by PossessorPlayerEntityMixin
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    protected void requiem$canWalkOnFluid(FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
        // overridden by PossessorPlayerEntityMixin
    }

    @Inject(
        method = "fall",
        at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/LivingEntity;fallDistance:F", ordinal = 0),
        cancellable = true
    )
    private void onFall(double fallY, boolean onGround, BlockState floorBlock, BlockPos floorPos, CallbackInfo info) {
        if (this.requiem$getWorld().isClient) return;

        Entity possessed = PossessionComponent.getHost((Entity) (Object) this);
        if (possessed != null && this.requiem$getFallDistance() > 0) {
            possessed.fallDistance = this.requiem$getFallDistance();
            possessed.copyPositionAndRotation((Entity) (Object) this);
            possessed.move(MovementType.SELF, Vec3d.ZERO);
            // We know that possessed is a LivingEntity, Mixin will translate to that type automatically
            //noinspection ConstantConditions
            ((PossessorLivingEntityMixin) (Object) possessed).requiem$fall(fallY, onGround, floorBlock, floorPos);
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

    private boolean requiem$wasSprinting;
    @Inject(method = "method_26317", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
    private void preventWaterHovering(double d, boolean bl, Vec3d vec3d, CallbackInfoReturnable<Vec3d> cir) {
        if (this.requiem$isSprinting() && MovementAlterer.KEY.maybeGet(this).map(MovementAlterer::disablesSwimming).orElse(false)) {
            requiem$wasSprinting = true;
            this.setSprinting(false);
        }
    }

    @Inject(method = "method_26317", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z", shift = At.Shift.AFTER))
    private void restoreSprint(double d, boolean bl, Vec3d vec3d, CallbackInfoReturnable<Vec3d> cir) {
        if (requiem$wasSprinting) {
            requiem$wasSprinting = false;
            this.setSprinting(true);
        }
    }

    @Inject(method = "sleep", at = @At("RETURN"))
    private void makeHostSleep(BlockPos pos, CallbackInfo ci) {
        LivingEntity host = PossessionComponent.getHost((Entity) (Object) this);
        if (host != null && host.getType().isIn(RequiemCoreTags.Entity.SLEEPERS)) {
            host.sleep(pos);
        }
    }

    @Inject(method = "wakeUp", at = @At("RETURN"))
    private void makeHostWakeUp(CallbackInfo ci) {
        LivingEntity host = PossessionComponent.getHost((Entity) (Object) this);
        if (host != null && host.getType().isIn(RequiemCoreTags.Entity.SLEEPERS)) {
            host.wakeUp();
        }
    }
}
