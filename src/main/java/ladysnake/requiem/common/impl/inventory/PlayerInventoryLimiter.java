package ladysnake.requiem.common.impl.inventory;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryPart;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;

import java.util.EnumSet;

public final class PlayerInventoryLimiter implements InventoryLimiter {
    public static final int MAINHAND_SLOT = 0;
    public static final int OFFHAND_SLOT = 40;

    private final PlayerEntity player;
    private final EnumSet<InventoryPart> lockedParts = EnumSet.allOf(InventoryPart.class);
    private boolean enabled;

    public PlayerInventoryLimiter(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled && !this.player.isCreative();
    }

    @Override
    public void lock(InventoryPart part) {
        this.lockedParts.add(part);
    }

    @Override
    public void unlock(InventoryPart part) {
        this.lockedParts.remove(part);
    }

    @Override
    public HotbarAvailability getHotbarAvailability() {
        if (!isEnabled()) return HotbarAvailability.FULL;
        if (this.lockedParts.contains(InventoryPart.HANDS)) return HotbarAvailability.NONE;
        return this.lockedParts.contains(InventoryPart.MAIN) ? HotbarAvailability.HANDS : HotbarAvailability.FULL;
    }

    @Override
    public boolean isSlotLocked(int index) {
        if (!isEnabled()) return false;

        int mainSize = player.inventory.main.size();

        if (this.lockedParts.contains(InventoryPart.MAIN) && index > MAINHAND_SLOT && index < mainSize) {
            return true;
        }

        int armorSize = player.inventory.armor.size();

        if (this.lockedParts.contains(InventoryPart.ARMOR) && index >= mainSize && index < mainSize + armorSize) {
            return true;
        }

        return this.lockedParts.contains(InventoryPart.HANDS) && (index == MAINHAND_SLOT || index == OFFHAND_SLOT);
    }

    @Override
    public boolean isSlotInvisible(int playerSlot) {
        return this.player.currentScreenHandler == this.player.playerScreenHandler
            && (this.isSlotLocked(playerSlot) || (playerSlot == MAINHAND_SLOT && this.isMainInventoryLocked()));
    }

    @Override
    public boolean useAlternativeInventory() {
        return this.isMainInventoryLocked() && PossessionComponent.get(this.player).isPossessing();
    }

    private boolean isMainInventoryLocked() {
        return this.isEnabled() && this.lockedParts.contains(InventoryPart.ARMOR);
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        if (compoundTag.contains("enabled")) {
            this.enabled = compoundTag.getBoolean("enabled");
        }
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        compoundTag.putBoolean("enabled", this.enabled);
    }
}
