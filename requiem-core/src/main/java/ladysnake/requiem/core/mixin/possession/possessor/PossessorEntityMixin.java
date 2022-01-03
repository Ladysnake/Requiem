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

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PossessorEntityMixin {

    @Invoker("isSprinting")
    public abstract boolean requiem$isSprinting();

    @Invoker("getX")
    protected abstract double requiem$getX();

    @Invoker("getY")
    protected abstract double requiem$getY();

    @Invoker("getZ")
    protected abstract double requiem$getZ();

    @Accessor("yaw")
    protected abstract float requiem$getYaw();

    @Accessor("pitch")
    public abstract float requiem$getPitch();

    @Accessor("horizontalCollision")
    protected abstract boolean requiem$isCollidingHorizontally();

    @Accessor("fallDistance")
    protected abstract float requiem$getFallDistance();

    @Accessor("fallDistance")
    public abstract void requiem$setFallDistance(float distance);

    @Accessor("world")
    protected abstract World requiem$getWorld();

    @Accessor("movementMultiplier")
    public abstract void requiem$setMovementMultiplier(Vec3d multiplier);

    @Invoker("fall")
    protected abstract void requiem$fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    /**
     * Delegates the air getter for possessing entities.
     */
    @Inject(method = "getAir", at = @At("HEAD"), cancellable = true)
    protected void requiem$delegateBreath(CallbackInfoReturnable<Integer> cir) {
        // overridden by PossessorPlayerEntityMixin
    }

    @Inject(method = "getMaxAir", at = @At("HEAD"), cancellable = true)
    protected void requiem$delegateMaxBreath(CallbackInfoReturnable<Integer> cir) {
        // overridden by PossessorPlayerEntityMixin
    }

    @Inject(method = "canAvoidTraps", at = @At("RETURN"), cancellable = true)
    protected void requiem$soulsAvoidTraps(CallbackInfoReturnable<Boolean> cir) {
        // overridden by PossessorPlayerEntityMixin
    }

    @Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true)
    protected void requiem$isOnFire(CallbackInfoReturnable<Boolean> cir) {
        // overridden by PossessorPlayerEntityMixin
    }
}
