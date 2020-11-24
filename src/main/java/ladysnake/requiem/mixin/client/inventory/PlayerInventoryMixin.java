package ladysnake.requiem.mixin.client.inventory;

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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
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

    @Unique
    private InventoryLimiter requiemLimiter;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(PlayerEntity player, CallbackInfo ci) {
        this.requiemLimiter = InventoryLimiter.KEY.get(player);
    }

    @Inject(method = {"addPickBlock", "scrollInHotbar", "clone"},
        at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", shift = At.Shift.AFTER)
    )
    private void preventHotbarSelection(CallbackInfo ci) {
        if (this.requiemLimiter.isSlotLocked(this.selectedSlot)) {
            this.selectedSlot = PlayerInventoryLimiter.MAINHAND_SLOT;
        }
    }

    @ModifyVariable(method = "getEmptySlot", at = @At(value = "LOAD", ordinal = 0))
    private int skipLockedSlots(int slot) {
        InventoryLimiter limiter = this.requiemLimiter;
        while (limiter.isSlotLocked(slot) && slot < this.main.size()) {
            slot++;
        }
        return slot;
    }

    @Inject(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void preventAddStack(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (this.requiemLimiter.isSlotLocked(slot)) {
            cir.setReturnValue(stack.getCount());
        }
    }

    @ModifyArg(
        method = "getOccupiedSlotWithRoomForStack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerInventory;canStackAddMore(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
            ordinal = 0
        ),
        index = 0
    )
    private ItemStack preventMainHandStackAttempt(ItemStack stack) {
        if (this.requiemLimiter.isSlotLocked(PlayerInventoryLimiter.MAINHAND_SLOT)) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @ModifyArg(
        method = "getOccupiedSlotWithRoomForStack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerInventory;canStackAddMore(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
            ordinal = 1
        ),
        index = 0
    )
    private ItemStack preventOffHandStackAttempt(ItemStack stack) {
        if (this.requiemLimiter.isSlotLocked(PlayerInventoryLimiter.OFFHAND_SLOT)) {
            return ItemStack.EMPTY;
        }
        return stack;
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
        at = @At(value = "LOAD", ordinal = 0)
    )
    private int preventStackAttempt(int slot) {
        InventoryLimiter limiter = this.requiemLimiter;
        while (limiter.isSlotLocked(slot) && slot < this.main.size()) {
            slot++;
        }
        return slot;
    }
}
