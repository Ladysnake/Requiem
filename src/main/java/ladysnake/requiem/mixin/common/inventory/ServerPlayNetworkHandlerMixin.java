package ladysnake.requiem.mixin.common.inventory;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.common.impl.inventory.PlayerInventoryLimiter;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onUpdateSelectedSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/UpdateSelectedSlotC2SPacket;getSelectedSlot()I", ordinal = 0))
    private void fixSelectedSlot(UpdateSelectedSlotC2SPacket packet, CallbackInfo ci) {
        if (InventoryLimiter.KEY.get(this.player).isSlotLocked(packet.getSelectedSlot())) {
            ((UpdateSelectedSlotC2SPacketAccessor) packet).setSelectedSlot(PlayerInventoryLimiter.MAINHAND_SLOT);
        }
    }
}
