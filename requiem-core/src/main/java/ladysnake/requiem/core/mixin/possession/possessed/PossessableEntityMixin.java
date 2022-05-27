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
package ladysnake.requiem.core.mixin.possession.possessed;

import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class PossessableEntityMixin implements ProtoPossessable {

    @Shadow
    public World world;

    @Shadow
    public boolean velocityModified;

    @Inject(method = "scheduleVelocityUpdate", at = @At("RETURN"))
    private void scheduleVelocityUpdate(CallbackInfo ci) {
        PlayerEntity player = this.getPossessor();
        if (player != null && !world.isClient && this.velocityModified) {
            player.velocityModified = true;
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = this.getPossessor();
        if (player != null && player.isCreative()) {
            cir.setReturnValue(!source.isOutOfWorld());
        }
    }

    @Inject(method = "canUsePortals", at = @At("HEAD"), cancellable = true)
    private void canUsePortals(CallbackInfoReturnable<Boolean> cir) {
        if (this.isBeingPossessed()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("HEAD"), cancellable = true)
    private void startRiding(Entity mount, boolean force, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = this.getPossessor();
        if (player != null) {
            cir.setReturnValue(player.startRiding(mount, force));
        }
    }

    @Inject(method = "calculateDimensions", at = @At("RETURN"))
    private void calculatePossessorDimensions(CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) possessor.calculateDimensions();
    }

    @Inject(method = "saveNbt", at = @At("HEAD"), cancellable = true)
    private void cancelPossessableSave(NbtCompound tag, CallbackInfoReturnable<Boolean> cir) {
        if (this.isBeingPossessed()) {
            cir.setReturnValue(false);
        }
    }
}
