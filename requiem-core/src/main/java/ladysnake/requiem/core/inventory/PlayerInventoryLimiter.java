/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.core.inventory;

import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.InventoryKeeper;
import io.github.ladysnake.locki.InventoryLock;
import io.github.ladysnake.locki.InventoryNode;
import io.github.ladysnake.locki.impl.PlayerInventoryKeeper;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryShape;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.player.PlayerEntity;

public final class PlayerInventoryLimiter implements InventoryLimiter {
    public static final int MAINHAND_SLOT = 0;

    private final InventoryLock lock;

    public PlayerInventoryLimiter(InventoryLock lock) {
        this.lock = lock;
    }

    @Override
    public InventoryLock getLock() {
        return this.lock;
    }

    @Override
    public void enable(PlayerEntity player) {
        if (!player.world.isClient) {
            lock.lockInventory(player);
        }
    }

    @Override
    public void disable(PlayerEntity player) {
        if (!player.world.isClient) {
            lock.unlockInventory(player);
        }
    }

    @Override
    public void lock(PlayerEntity player, InventoryNode part) {
        if (!player.world.isClient) {
            lock.lock(player, part);
        }
    }

    @Override
    public void unlock(PlayerEntity player, InventoryNode part) {
        if (!player.world.isClient) {
            lock.unlock(player, part);
        }
    }

    @Override
    public boolean isLocked(PlayerEntity player, InventoryNode part) {
        return InventoryKeeper.get(player).isLocked(part);
    }

    @Override
    public boolean isSlotLocked(PlayerEntity player, int index) {
        return InventoryKeeper.get(player).isSlotLocked(index);
    }

    @Override
    public boolean isSlotInvisible(PlayerEntity player, int playerSlot) {
        InventoryShape inventoryShape = this.getInventoryShape(player);
        return switch (playerSlot) {
            // BackSlot's extra slots basically cannot be made invisible
            case PlayerInventoryKeeper.BACK_SLOT, PlayerInventoryKeeper.BELT_SLOT -> inventoryShape == InventoryShape.ALT_LARGE;
            default -> player.currentScreenHandler == player.playerScreenHandler
                && inventoryShape != InventoryShape.NORMAL
                && (inventoryShape != InventoryShape.ALT_SMALL || playerSlot >= 9)
                && (this.isSlotLocked(player, playerSlot) || (playerSlot == MAINHAND_SLOT && inventoryShape == InventoryShape.ALT));
        };
    }

    @Override
    public InventoryShape getInventoryShape(PlayerEntity player) {
        if (!player.isCreative() && PossessionComponent.get(player).isPossessionOngoing()) {
            InventoryKeeper keeper = InventoryKeeper.get(player);
            if (keeper.isEntirelyLocked(DefaultInventoryNodes.INVENTORY)) {
                return InventoryShape.ALT_LARGE;
            } else if (keeper.isEntirelyLocked(DefaultInventoryNodes.MAIN_INVENTORY)) {
                return InventoryShape.ALT;
            }
            return InventoryShape.ALT_SMALL;
        }
        return InventoryShape.NORMAL;
    }
}
