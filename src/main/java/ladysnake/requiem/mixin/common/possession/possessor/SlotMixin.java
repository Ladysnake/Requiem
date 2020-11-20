package ladysnake.requiem.mixin.common.possession.possessor;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow
    @Final
    private int index;
    @Unique
    private @Nullable InventoryLimiter limiter;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(Inventory inventory, int index, int x, int y, CallbackInfo ci) {
        if (inventory instanceof PlayerInventory) {
            this.limiter = InventoryLimiter.KEY.get(((PlayerInventory) inventory).player);
        }
    }

    @Unique
    private boolean shouldBeLocked() {
        return this.limiter != null && this.limiter.isSlotLocked(this.index);
    }

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.shouldBeLocked()) cir.setReturnValue(false);
    }

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void canTakeItems(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir) {
        if (this.shouldBeLocked()) cir.setReturnValue(false);
    }
}
