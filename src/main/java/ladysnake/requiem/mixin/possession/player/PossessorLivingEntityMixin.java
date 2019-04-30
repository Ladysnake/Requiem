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

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.tag.RequiemEntityTags;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PossessorLivingEntityMixin extends Entity {
    public PossessorLivingEntityMixin(EntityType<?> entityType_1, World world_1) {
        super(entityType_1, world_1);
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
        if (this instanceof RequiemPlayer) {
            return ((RequiemPlayer) this).getMovementAlterer().getSwimmingAcceleration(speedAmount);
        }
        return speedAmount;
    }

    /**
     * Marks possessed entities as the attacker for any damage caused by their possessor
     *
     * @param source damage dealt
     * @param amount amount of damage dealt
     * @param info   callback
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void proxyDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (source.getAttacker() instanceof RequiemPlayer) {
            Entity possessed = ((RequiemPlayer) source.getAttacker()).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                DamageSource newSource = null;
                if (source instanceof ProjectileDamageSource)
                    newSource = new ProjectileDamageSource(source.getName(), source.getSource(), possessed);
                else if (source instanceof EntityDamageSource)
                    newSource = new EntityDamageSource(source.getName(), possessed);
                if (newSource != null) {
                    ((LivingEntity) (Object) this).damage(newSource, amount);
                    info.setReturnValue(true);
                }
            }
        }
    }

    @Inject(method = "collides", at = @At("RETURN"), cancellable = true)
    private void preventSoulsCollision(CallbackInfoReturnable<Boolean> info) {
        if (this instanceof RequiemPlayer && ((RequiemPlayer) this).getRemnantState().isSoul()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "isClimbing", at = @At("HEAD"), cancellable = true)
    private void canClimb(CallbackInfoReturnable<Boolean> info) {
        if (this instanceof RequiemPlayer && this.horizontalCollision) {
            LivingEntity possessed = ((RequiemPlayer) this).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                info.setReturnValue(RequiemEntityTags.CLIMBER.contains(possessed.getType()));
            }
        }
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    private void adjustEyeHeight(EntityPose pose, EntitySize size, CallbackInfoReturnable<Float> cir) {
        if (this instanceof RequiemPlayer) {
            PossessionComponent possessionComponent = ((RequiemPlayer) this).getPossessionComponent();
            // This method can be called before the possession component is set
            //noinspection ConstantConditions
            if (possessionComponent != null) {
                LivingEntity possessed = possessionComponent.getPossessedEntity();
                if (possessed != null) {
                    cir.setReturnValue(possessed.getEyeHeight(pose));
                }
            }
        }
    }
}
