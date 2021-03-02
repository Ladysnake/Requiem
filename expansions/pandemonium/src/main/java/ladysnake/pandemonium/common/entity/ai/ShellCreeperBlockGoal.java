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
import ladysnake.pandemonium.mixin.common.item.ItemAccessor;
import ladysnake.requiem.common.tag.RequiemItemTags;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class ShellCreeperBlockGoal extends Goal {
    private final PlayerShellEntity shell;
    private int shieldSlot = Integer.MIN_VALUE;
    private int waterSlot = Integer.MIN_VALUE;
    private @Nullable BlockPos placedWater;
    private @Nullable CreeperEntity primedCreeper;

    public ShellCreeperBlockGoal(PlayerShellEntity shell) {
        this.shell = shell;
        this.setControls(EnumSet.of(Control.MOVE, Control.JUMP, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        Box box = Box.method_29968(this.shell.getPos()).expand(4);
        Item item = this.shell.getOffHandStack().getItem();
        this.shieldSlot = Integer.MIN_VALUE;
        this.waterSlot = Integer.MIN_VALUE;

        if (RequiemItemTags.SHIELDS.contains(item)) {
            shieldSlot = -1;
        } else if (RequiemItemTags.WATER_BUCKETS.contains(item)) {
            waterSlot = -1;
        }

        for (int slot = 0; slot < 9; slot++) {
            item = this.shell.inventory.getStack(slot).getItem();
            if (RequiemItemTags.SHIELDS.contains(item)) {
                shieldSlot = slot;
            } else if (RequiemItemTags.WATER_BUCKETS.contains(item)) {
                waterSlot = slot;
            }
        }

        if (this.shieldSlot == Integer.MIN_VALUE) return false;

        // Catch all creepers that are about to explode
        List<CreeperEntity> creepers = this.shell.world.getEntitiesIncludingUngeneratedChunks(CreeperEntity.class, box, c -> c.getFuseSpeed() > 0 && ((CreeperEntityAccessor) c).getCurrentFuseTime() > 15);
        if (creepers.isEmpty()) return false;
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

        // If the water bucket was in the offhand, it is now in the main hand -> no need to change
        if (this.waterSlot >= 0) {
            this.shell.selectHotbarSlot(this.waterSlot);
        }

        this.placedWater = null;
    }

    @Override
    public boolean shouldContinue() {
        return this.primedCreeper != null && this.primedCreeper.isAlive() && this.primedCreeper.getFuseSpeed() > 0;
    }

    @Override
    public void tick() {
        if (this.primedCreeper == null) return;

        CreeperEntity creeper = this.primedCreeper;
        this.shell.getGuide().getLookControl().lookAt(creeper.getX()+0.5, creeper.getY()-0.5, creeper.getZ()+0.5, 180, 180);
        int fuseTime = ((CreeperEntityAccessor) creeper).getCurrentFuseTime();
        World world = this.shell.world;
        BlockHitResult rayResult = ItemAccessor.invokeRaycast(world, this.shell, RaycastContext.FluidHandling.NONE);
        BlockPos creeperPos = creeper.getBlockPos();
        BlockPos pos = creeperPos.down();

        if (fuseTime > 6 && rayResult.getType() == HitResult.Type.BLOCK && rayResult.getBlockPos().equals(pos) && rayResult.getSide() == Direction.UP && RequiemItemTags.WATER_BUCKETS.contains(this.shell.getMainHandStack().getItem()) && world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            this.shell.useItem(Hand.MAIN_HAND);
        } else {
            this.shell.useItem(Hand.OFF_HAND);
        }
    }

    @Override
    public void stop() {
        this.primedCreeper = null;
        this.shell.clearActiveItem();

        // Clean up the mess
        if (this.placedWater != null) {
            this.shell.getGuide().getLookControl().lookAt(placedWater.getX()+0.5, placedWater.getY()+0.5, placedWater.getZ()+0.5, 180, 180);
            BlockHitResult rayResult = ItemAccessor.invokeRaycast(this.shell.world, this.shell, RaycastContext.FluidHandling.SOURCE_ONLY);

            if (rayResult.getType() == HitResult.Type.BLOCK && this.shell.world.getBlockState(rayResult.getBlockPos()).getBlock() instanceof FluidDrainable) {
                this.shell.useItem(Hand.MAIN_HAND);
            }
            this.placedWater = null;
        }
    }
}
