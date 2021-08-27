/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.mixin.client.possession.riding;

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.api.v1.event.minecraft.JumpingMountEvents;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.core.mixin.access.LivingEntityAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * But what happens if a mod calls this method then assumes mount != null ?
     * Well that would be very sad and I'd tell that mod to add an instance check :P
     */
    @Inject(method = "hasJumpingMount", at = @At("RETURN"), cancellable = true)
    private void hackJumpingMount(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && this.getVehicle() == null) {
            LivingEntity host = PossessionComponent.getHost(this);
            if (host != null) {
                JumpingMount jumpingMount = JumpingMountEvents.MOUNT_CHECK.invoker().getJumpingMount(host);
                if (jumpingMount != null && jumpingMount.canJump()) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @ModifyVariable(method = "tickMovement", at = @At("STORE"))
    private JumpingMount swapJumpingMount(JumpingMount baseMount) {
        if (baseMount == null) {
            LivingEntity host = PossessionComponent.getHost(this);
            if (host != null) {
                JumpingMount jumpingMount = JumpingMountEvents.MOUNT_CHECK.invoker().getJumpingMount(host);
                if (jumpingMount != null) {
                    // prevent normal jumps
                    ((LivingEntityAccessor) this).setJumpingCooldown(10);
                    return jumpingMount;
                }
            }
        }
        return baseMount;
    }
}
