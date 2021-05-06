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
package ladysnake.pandemonium.common.entity.ai;

import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.util.Hand;

public class ShellEatGoal extends PlayerShellGoal {
    private int previousSlot;

    public ShellEatGoal(PlayerShellEntity shell) {
        super(shell);
    }

    @Override
    public boolean canStart() {
        return this.shell.getHungerManager().isNotFull() && this.findInHotbar(itemStack -> {
            FoodComponent foodComponent = itemStack.getItem().getFoodComponent();
            // We do not want to eat special food
            return foodComponent != null && !foodComponent.isAlwaysEdible() && foodComponent.getStatusEffects().isEmpty();
        });
    }

    @Override
    public boolean shouldContinue() {
        return this.canStart();
    }

    @Override
    public void start() {
        if (this.hotbarSlot >= 0) {
            this.previousSlot = this.shell.inventory.selectedSlot;
            this.shell.selectHotbarSlot(this.hotbarSlot);
            this.shell.useItem(Hand.MAIN_HAND);
        } else {
            this.previousSlot = -1;
            this.shell.useItem(Hand.OFF_HAND);
        }
    }

    @Override
    public void stop() {
        this.shell.releaseActiveItem();

        if (this.previousSlot >= 0) {
            this.shell.selectHotbarSlot(this.previousSlot);
        }
    }
}
