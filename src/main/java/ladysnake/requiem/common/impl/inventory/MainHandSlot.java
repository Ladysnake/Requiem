package ladysnake.requiem.common.impl.inventory;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;

public class MainHandSlot extends Slot {
    private final InventoryLimiter limiter;

    public MainHandSlot(PlayerEntity owner, int x, int y) {
        super(owner.inventory, 0, x, y);
        limiter = InventoryLimiter.KEY.get(owner);
    }

    @Override
    public boolean doDrawHoveringEffect() {
        return !limiter.isSlotLocked(PlayerInventoryLimiter.MAINHAND_SLOT) && limiter.isSlotInvisible(PlayerInventoryLimiter.MAINHAND_SLOT);
    }
}
