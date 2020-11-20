package ladysnake.requiem.mixin.common.possession.possessor;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Inject(
        method = "method_30010",
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/screen/slot/SlotActionType;SWAP:Lnet/minecraft/screen/slot/SlotActionType;"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/screen/slot/SlotActionType;CLONE:Lnet/minecraft/screen/slot/SlotActionType;")
        ),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getStack(I)Lnet/minecraft/item/ItemStack;"),
        cancellable = true
    )
    private void preventSwap(int screenSlot, int playerSlot, SlotActionType slotActionType, PlayerEntity playerEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (InventoryLimiter.KEY.get(playerEntity).isSlotLocked(playerSlot)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
