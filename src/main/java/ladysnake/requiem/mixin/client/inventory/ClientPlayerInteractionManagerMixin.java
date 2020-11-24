package ladysnake.requiem.mixin.client.inventory;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.common.impl.inventory.PlayerInventoryLimiter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "syncSelectedSlot", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I"))
    private void fixSelectedSlot(CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        assert player != null;

        if (InventoryLimiter.KEY.get(player).isSlotLocked(player.inventory.selectedSlot)) {
            player.inventory.selectedSlot = PlayerInventoryLimiter.MAINHAND_SLOT;
        }
    }
}
