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
package ladysnake.requiem.mixin.common.remnant;

import ladysnake.requiem.api.v1.event.minecraft.AllowUseEntityCallback;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;interact(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"), cancellable = true)
    private void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo info) {
        checkInteraction(packet, info);
    }

    // Injecting before the FAPI event
    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interactAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", shift = At.Shift.BEFORE), cancellable = true)
    private void onPlayerInteractEntity2(PlayerInteractEntityC2SPacket packet, CallbackInfo info) {
        checkInteraction(packet, info);
    }

    @Unique
    private void checkInteraction(PlayerInteractEntityC2SPacket packet, CallbackInfo info) {
        World world = player.getEntityWorld();
        Entity entity = packet.getEntity(world);

        if (entity != null) {
            assert packet.getHand() != null;
            boolean result = AllowUseEntityCallback.EVENT.invoker().allow(player, world, packet.getHand(), entity);

            if (!result) {
                info.cancel();
            }
        }
    }

    @Inject(method = "onClientStatus", at = @At(value = "FIELD", target = "Lnet/minecraft/world/GameMode;SPECTATOR:Lnet/minecraft/world/GameMode;"), cancellable = true)
    private void postponeHardcoreConsequences(ClientStatusC2SPacket packet, CallbackInfo ci) {
        if (RemnantComponent.isVagrant(this.player)) {
            ci.cancel();
        }
    }
}
