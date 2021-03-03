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
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class ShellBlockGoal<E extends Entity> extends Goal {
    protected final PlayerShellEntity shell;
    private final int searchRadius;
    private final Class<? extends E> targetClass;
    private final Comparator<E> comparator;
    private final Predicate<E> candidatePredicate;
    private int shieldSlot;
    protected @Nullable E target;

    public ShellBlockGoal(PlayerShellEntity shell, Class<E> targetClass, Comparator<E> comparator, Predicate<E> candidatePredicate, int searchRadius) {
        this.shell = shell;
        this.targetClass = targetClass;
        this.comparator = comparator;
        this.candidatePredicate = candidatePredicate;
        this.setControls(EnumSet.of(Control.MOVE, Control.JUMP, Control.LOOK));
        this.searchRadius = searchRadius;
    }

    @Override
    public boolean canStart() {
        if (!this.findShield()) {
            return false;
        }

        Box box = Box.method_29968(this.shell.getPos()).expand(searchRadius);
        List<E> candidates = this.shell.world.getEntitiesIncludingUngeneratedChunks(
            this.targetClass,
            box,
            this.candidatePredicate
        );

        if (candidates.isEmpty()) {
            return false;
        }

        // Target the closest candidate
        this.target = Collections.min(candidates, this.comparator);
        return true;    // min cannot return null
    }

    private boolean findShield() {
        this.shieldSlot = -1;

        if (RequiemItemTags.SHIELDS.contains(this.shell.getOffHandStack().getItem())) {
            return true;
        }

        for (int slot = 0; slot < 9; slot++) {
            if (RequiemItemTags.SHIELDS.contains(this.shell.inventory.getStack(slot).getItem())) {
                shieldSlot = slot;
                return true;
            }
        }

        return false;
    }

    @Override
    public void start() {
        if (this.shieldSlot >= 0) {
            this.shell.selectHotbarSlot(this.shieldSlot);
            this.shell.swapHands();
        }
    }

    @Override
    public boolean shouldContinue() {
        return RequiemItemTags.SHIELDS.contains(this.shell.getOffHandStack().getItem())
            && this.target != null
            && this.target.isAlive()
            && this.candidatePredicate.test(this.target);
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        this.shell.getGuide().getLookControl().lookAt(this.target, 90, 90);
        this.shell.useItem(Hand.OFF_HAND);
    }

    @Override
    public void stop() {
        this.shell.clearActiveItem();
        this.target = null;
    }

    public static ShellBlockGoal<ProjectileEntity> blockProjectiles(PlayerShellEntity shell) {
        final int searchRadius = 10;

        return new ShellBlockGoal<>(
            shell,
            ProjectileEntity.class,
            Comparator.comparing(shell::squaredDistanceTo),
            candidate -> {
                Box dangerZone = shell.getBoundingBox().expand(1.5);
                if (dangerZone.intersects(candidate.getBoundingBox())) return true;

                Vec3d start = candidate.getPos();
                // Fun fact, projectile rotation is completely broken so we cannot use that
                Vec3d end = start.add(candidate.getVelocity().normalize().multiply(searchRadius));
                return dangerZone.raycast(start, end).isPresent();
            },
            searchRadius
        );
    }

    public static ShellBlockGoal<CreeperEntity> blockCreepers(PlayerShellEntity shell) {
        final int searchRadius = 5;

        return new ShellBlockGoal<>(
            shell,
            CreeperEntity.class,
            Comparator.comparing(c -> ((CreeperEntityAccessor) c).getCurrentFuseTime()),
            candidate -> candidate.getFuseSpeed() > 0 && ((CreeperEntityAccessor) candidate).getCurrentFuseTime() > 15,
            searchRadius
        );
    }
}
