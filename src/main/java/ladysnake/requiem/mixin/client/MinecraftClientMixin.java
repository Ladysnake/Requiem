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
package ladysnake.requiem.mixin.client;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public ClientPlayerEntity player;

    @Shadow public ClientPlayerInteractionManager interactionManager;

    @Inject(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;"), cancellable = true)
    private void tryUseDirectAttackAbility(CallbackInfo ci) {
        if (RequiemClient.INSTANCE.getTargetHandler().useDirectAbility(AbilityType.ATTACK)) {
            this.player.swingHand(Hand.MAIN_HAND);
            ci.cancel();
        }
    }

    @Inject(
            method = "doAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;resetLastAttackedTicks()V"
            )
    )
    private void onShakeFistAtAir(CallbackInfo info) {
        if (PossessionComponent.get( player).isPossessing()) {
            RequiemNetworking.sendAbilityUseMessage(AbilityType.ATTACK);
        }
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"), cancellable = true)
    private void tryUseDirectAbility(CallbackInfo ci) {
        if (RequiemClient.INSTANCE.getTargetHandler().useDirectAbility(AbilityType.INTERACT)) {
            this.player.swingHand(Hand.OFF_HAND);
            ci.cancel();
        }
    }

    /**
     * Calls special interact abilities when the player cannot interact with anything else
     */
    @Inject(method = "doItemUse", at=@At("TAIL"))
    private void onInteractWithAir(CallbackInfo info) {
        // Check that the player is qualified to interact with something
        if (!this.interactionManager.isBreakingBlock() && !this.player.isRiding()) {
            if (PossessionComponent.get(player).isPossessing() && player.getMainHandStack().isEmpty()) {
                RequiemNetworking.sendAbilityUseMessage(AbilityType.INTERACT);
            }
        }
    }

    @Inject(
            method = "openScreen",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0, opcode = Opcodes.PUTFIELD),
            cancellable = true
    )
    private void skipDeathScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DeathScreen) {
            if (RemnantComponent.get(this.player).getRemnantType().isDemon()) {
                this.player.requestRespawn();
                ci.cancel();
            }
        }
    }
}
