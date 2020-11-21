package ladysnake.requiem.common.impl.inventory;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

public final class PlayerInventoryLimiter implements InventoryLimiter {
    public static final int MAINHAND_SLOT = 0;

    private final PlayerEntity player;
    private boolean mainInventoryLocked;

    public PlayerInventoryLimiter(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void lockMainInventory(boolean lock) {
        this.mainInventoryLocked = lock;
    }

    @Override
    public boolean isMainInventoryLocked() {
        return !this.player.isCreative() && this.mainInventoryLocked;
    }

    @Override
    public boolean isSlotLocked(int index) {
        // First slot is the main hand
        return this.isMainInventoryLocked() && index > MAINHAND_SLOT && index < player.inventory.main.size();
    }

    @Override
    public boolean isSlotInvisible(int playerSlot) {
        return this.player.currentScreenHandler == this.player.playerScreenHandler
            && (this.isSlotLocked(playerSlot) || (playerSlot == MAINHAND_SLOT && this.isMainInventoryLocked()));
    }

    @Override
    public boolean useAlternativeInventory() {
        return this.isMainInventoryLocked();
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {

    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {

    }
}
