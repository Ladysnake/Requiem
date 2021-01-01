/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.impl.inventory;

import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryPart;
import ladysnake.requiem.api.v1.entity.InventoryShape;
import ladysnake.requiem.api.v1.event.requiem.InventoryLockingChangeCallback;
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
        if (this.lockedParts.add(part)) {
            InventoryLockingChangeCallback.EVENT.invoker().onInventoryLockingChange(this.player, part, true);
        }
    }

    @Override
    public void unlock(InventoryPart part) {
        if (this.lockedParts.remove(part)) {
            InventoryLockingChangeCallback.EVENT.invoker().onInventoryLockingChange(this.player, part, false);
        }
    }

    @Override
    public boolean isLocked(InventoryPart part) {
        return this.isEnabled() && this.lockedParts.contains(part);
    }

    @Override
    public HotbarAvailability getHotbarAvailability() {
        if (this.isLocked(InventoryPart.HANDS)) return HotbarAvailability.NONE;
        return this.isLocked(InventoryPart.MAIN) ? HotbarAvailability.HANDS : HotbarAvailability.FULL;
    }

    @Override
    public boolean isSlotLocked(int index) {
        if (!isEnabled()) return false;

        int mainSize = player.inventory.main.size();

        if (this.isLocked(InventoryPart.MAIN) && index > MAINHAND_SLOT && index < mainSize) {
            return true;
        }

        int armorSize = player.inventory.armor.size();

        if (this.isLocked(InventoryPart.ARMOR) && index >= mainSize && index < mainSize + armorSize) {
            return true;
        }

        return this.isLocked(InventoryPart.HANDS) && (index == MAINHAND_SLOT || index == OFFHAND_SLOT);
    }

    @Override
    public boolean isSlotInvisible(int playerSlot) {
        return this.player.currentScreenHandler == this.player.playerScreenHandler
            && this.getInventoryShape() != InventoryShape.NORMAL
            && (this.isSlotLocked(playerSlot) || (playerSlot == MAINHAND_SLOT && this.isMainInventoryLocked()));
    }

    @Override
    public InventoryShape getInventoryShape() {
        if (this.isEnabled()) {
            if (PossessionComponent.get(this.player).isPossessing()) {
                if (this.lockedParts.size() == InventoryPart.VALUES.size()) {
                    return InventoryShape.ALT_LARGE;
                } else if (this.lockedParts.contains(InventoryPart.MAIN)) {
                    return InventoryShape.ALT;
                }
                return InventoryShape.ALT_SMALL;
            }
//            return InventoryShape.ALT_SMALL;
        }
        return InventoryShape.NORMAL;
    }

    private boolean isMainInventoryLocked() {
        return this.isEnabled() && this.lockedParts.contains(InventoryPart.MAIN);
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
