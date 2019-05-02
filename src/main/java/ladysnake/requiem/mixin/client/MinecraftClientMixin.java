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
package ladysnake.requiem.mixin.client;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ingame.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ladysnake.requiem.common.network.RequiemNetworking.*;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public ClientPlayerEntity player;

    @Shadow public ClientPlayerInteractionManager interactionManager;

    @Inject(
            method = "doAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;method_7350()V"
            )
    )
    private void onShakeFistAtAir(CallbackInfo info) {
        if (((RequiemPlayer) player).getPossessionComponent().isPossessing()) {
            sendToServer(LEFT_CLICK_AIR, createEmptyBuffer());
        }
    }

    /**
     * Calls special interact abilities when the player cannot interact with anything else
     */
    @Inject(method = "doItemUse", at=@At("TAIL"))
    private void onInteractWithAir(CallbackInfo info) {
        // Check that the player is qualified to interact with something
        if (!this.interactionManager.isBreakingBlock() && !this.player.isRiding()) {
            if (((RequiemPlayer) player).getPossessionComponent().isPossessing() && player.getMainHandStack().isEmpty()) {
                sendToServer(RIGHT_CLICK_AIR, createEmptyBuffer());
            }
        }
    }

    @Inject(
            method = "openScreen",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/Screen;", ordinal = 0, opcode = Opcodes.PUTFIELD),
            cancellable = true
    )
    private void skipDeathScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DeathScreen) {
            if (RemnantState.getIfRemnant(this.player).isPresent()) {
                this.player.requestRespawn();
                ci.cancel();
            }
        }
    }
}
