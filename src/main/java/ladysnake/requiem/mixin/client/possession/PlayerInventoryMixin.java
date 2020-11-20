package ladysnake.requiem.mixin.client.possession;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.common.impl.inventory.PlayerInventoryLimiter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow
    public int selectedSlot;

    @Shadow
    @Final
    public PlayerEntity player;

    @Shadow
    @Final
    public DefaultedList<ItemStack> main;

    @Inject(method = {"addPickBlock", "scrollInHotbar", "clone"},
        at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", shift = At.Shift.AFTER)
    )
    private void preventHotbarSelection(CallbackInfo ci) {
        if (InventoryLimiter.KEY.get(this.player).isSlotLocked(this.selectedSlot)) {
            this.selectedSlot = PlayerInventoryLimiter.MAINHAND_SLOT;
        }
    }

    @Inject(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void preventAddStack(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (InventoryLimiter.KEY.get(this.player).isSlotLocked(slot)) {
            cir.setReturnValue(stack.getCount());
        }
    }

    /**
     * If a player somehow gets a stackable in a locked inventory slot,
     * any future attempt to insert the same item into your inventory will fail.
     *
     * <p>This injection prevents the stacking attempt, letting items go to empty slots in the aforementioned
     * scenario.
     */
    @ModifyVariable(
        method = "getOccupiedSlotWithRoomForStack",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;canStackAddMore(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", ordinal = 1),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;canStackAddMore(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", ordinal = 2)
        ),
        at = @At(value = "STORE", ordinal = 0)
    )
    private int preventStackAttempt(int slot) {
        if (InventoryLimiter.KEY.get(this.player).isMainInventoryLocked()) {
            return this.main.size();
        }
        return slot;
    }
}
