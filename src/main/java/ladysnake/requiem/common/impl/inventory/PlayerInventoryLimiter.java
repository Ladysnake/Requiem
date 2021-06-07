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

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.ImmutableMap;
import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryKeeper;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.InventoryLockingChangeCallback;
import io.github.ladysnake.locki.InventoryNode;
import io.github.ladysnake.locki.Locki;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryPart;
import ladysnake.requiem.api.v1.entity.InventoryShape;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public final class PlayerInventoryLimiter implements InventoryLimiter {
    public static final int MAINHAND_SLOT = 0;

    private static final BiMap<InventoryPart, InventoryNode> lockiMappings = EnumHashBiMap.create(ImmutableMap.of(
        InventoryPart.MAIN, DefaultInventoryNodes.MAIN_INVENTORY,
        InventoryPart.HANDS, DefaultInventoryNodes.HANDS,
        InventoryPart.ARMOR, DefaultInventoryNodes.ARMOR,
        InventoryPart.CRAFTING, DefaultInventoryNodes.CRAFTING
    ));

    private static final InventoryLock lock = Locki.registerLock(Requiem.id("inventory_limiter"));

    static {
        InventoryLockingChangeCallback.EVENT.register((player, part, l) -> ladysnake.requiem.api.v1.event.requiem.InventoryLockingChangeCallback.EVENT.invoker().onInventoryLockingChange(player, lockiMappings.inverse().get(part), l));
    }

    private final PlayerEntity player;

    public PlayerInventoryLimiter(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!this.player.world.isClient) {
            if (enabled) lock.lockInventory(this.player);
            else lock.unlockInventory(this.player);
        }
    }

    @Override
    public boolean isEnabled() {
        return lock.isLocking(player, DefaultInventoryNodes.INVENTORY) && !this.player.isCreative();
    }

    @Override
    public void lock(InventoryPart part) {
        if (!this.player.world.isClient) {
            lock.lock(this.player, lockiMappings.get(part));
        }
    }

    @Override
    public void unlock(InventoryPart part) {
        if (!this.player.world.isClient) {
            lock.unlock(this.player, lockiMappings.get(part));
        }
    }

    @Override
    public boolean isLocked(InventoryPart part) {
        return InventoryKeeper.get(this.player).isLocked(lockiMappings.get(part));
    }

    @Override
    public HotbarAvailability getHotbarAvailability() {
        if (this.isLocked(InventoryPart.HANDS)) return HotbarAvailability.NONE;
        return this.isLocked(InventoryPart.MAIN) ? HotbarAvailability.HANDS : HotbarAvailability.FULL;
    }

    @Override
    public boolean isSlotLocked(int index) {
        return InventoryKeeper.get(this.player).isSlotLocked(index);
    }

    @Override
    public boolean isSlotInvisible(int playerSlot) {
        InventoryShape inventoryShape = this.getInventoryShape();
        return this.player.currentScreenHandler == this.player.playerScreenHandler
            && inventoryShape != InventoryShape.NORMAL
            && (inventoryShape != InventoryShape.ALT_SMALL || playerSlot >= 9)
            && (this.isSlotLocked(playerSlot) || (playerSlot == MAINHAND_SLOT && inventoryShape == InventoryShape.ALT));
    }

    @Override
    public InventoryShape getInventoryShape() {
        if (!player.isCreative() && PossessionComponent.get(this.player).isPossessing()) {
            InventoryKeeper keeper = InventoryKeeper.get(this.player);
            if (keeper.isEntirelyLocked(DefaultInventoryNodes.INVENTORY)) {
                return InventoryShape.ALT_LARGE;
            } else if (keeper.isLocked(DefaultInventoryNodes.MAIN_INVENTORY)) {
                return InventoryShape.ALT;
            }
            return InventoryShape.ALT_SMALL;
        }
        return InventoryShape.NORMAL;
    }

    private boolean isMainInventoryLocked() {
        return lock.isLocking(this.player, DefaultInventoryNodes.MAIN_INVENTORY);
    }

    @Override
    public void readFromNbt(NbtCompound compoundTag) {
        if (compoundTag.contains("enabled")) {
            // compat with old saves
            this.setEnabled(compoundTag.getBoolean("enabled"));
        }
    }

    @Override
    public void writeToNbt(NbtCompound compoundTag) {
        // NO-OP
    }
}
