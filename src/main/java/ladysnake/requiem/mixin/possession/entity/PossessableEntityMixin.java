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
package ladysnake.requiem.mixin.possession.entity;

import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class PossessableEntityMixin implements ProtoPossessable {

    /* * * * * * * * * * * * * * * * *
      ProtoPossessable implementation
     * * * * * * * * * * * * * * * * */

    @Nullable
    @Override
    public PlayerEntity getPossessor() {
        return null;
    }

    @Override
    public boolean isBeingPossessed() {
        return false;
    }

    /* * * * * * *
      Injections
     * * * * * * */

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = this.getPossessor();
        if (player != null && (player.isCreative() || player == source.getAttacker())) {
            cir.setReturnValue(!source.doesDamageToCreative());
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
}
