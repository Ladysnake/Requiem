package ladysnake.dissolution.mixin.client;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
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

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;

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
        if (((DissolutionPlayer) player).getPossessionComponent().isPossessing()) {
            sendToServer(createLeftClickMessage());
        }
    }

    /**
     * Calls special interact abilities when the player cannot interact with anything else
     */
    @Inject(method = "doItemUse", at=@At("TAIL"))
    private void onInteractWithAir(CallbackInfo info) {
        // Check that the player is qualified to interact with something
        if (!this.interactionManager.isBreakingBlock() && !this.player.method_3144()) {
            if (((DissolutionPlayer) player).getPossessionComponent().isPossessing() && player.getMainHandStack().isEmpty()) {
                sendToServer(createRightClickMessage());
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
