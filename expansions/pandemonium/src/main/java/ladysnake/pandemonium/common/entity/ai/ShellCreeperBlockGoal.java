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
import ladysnake.pandemonium.mixin.common.entity.mob.CreeperEntityAccessor;
import ladysnake.requiem.common.tag.RequiemItemTags;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class ShellCreeperBlockGoal extends Goal {
    private final PlayerShellEntity shell;
    private int shieldSlot;
    private @Nullable CreeperEntity primedCreeper;

    public ShellCreeperBlockGoal(PlayerShellEntity shell) {
        this.shell = shell;
        this.setControls(EnumSet.of(Control.MOVE, Control.JUMP, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        Box box = Box.method_29968(this.shell.getPos()).expand(5);
        this.shieldSlot = -1;

        shieldSearch:
        if (!RequiemItemTags.SHIELDS.contains(this.shell.getOffHandStack().getItem())) {
            for (int slot = 0; slot < 9; slot++) {
                if (RequiemItemTags.SHIELDS.contains(this.shell.inventory.getStack(slot).getItem())) {
                    shieldSlot = slot;
                    break shieldSearch;
                }
            }
        }

        // Catch all creepers that are about to explode
        List<CreeperEntity> creepers = this.shell.world.getEntitiesIncludingUngeneratedChunks(CreeperEntity.class, box, c -> c.getFuseSpeed() > 0 && ((CreeperEntityAccessor) c).getCurrentFuseTime() > 15);
        if (creepers.isEmpty()) return false;
        // Target the most imminent explosion
        this.primedCreeper = Collections.min(creepers, Comparator.comparing(c -> ((CreeperEntityAccessor)c).getCurrentFuseTime()));
        return this.primedCreeper != null;
    }

    @Override
    public void start() {
        if (this.primedCreeper == null) return;

        if (this.shieldSlot >= 0) {
            this.shell.selectHotbarSlot(this.shieldSlot);
            this.shell.swapHands();
        }
    }

    @Override
    public boolean shouldContinue() {
        return this.primedCreeper != null && this.primedCreeper.isAlive() && this.primedCreeper.getFuseSpeed() > 0;
    }

    @Override
    public void tick() {
        if (this.primedCreeper == null) return;

        this.shell.getGuide().getLookControl().lookAt(this.primedCreeper, 90, 90);
        this.shell.useItem(Hand.OFF_HAND);
    }

    @Override
    public void stop() {
        this.primedCreeper = null;
        this.shell.clearActiveItem();
    }
}
