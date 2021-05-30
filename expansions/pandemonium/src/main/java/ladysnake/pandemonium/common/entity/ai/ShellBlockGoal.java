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
import ladysnake.pandemonium.mixin.common.entity.PersistentProjectileEntityAccessor;
import ladysnake.pandemonium.mixin.common.entity.mob.CreeperEntityAccessor;
import ladysnake.requiem.common.tag.RequiemItemTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
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

public class ShellBlockGoal<E extends Entity> extends PlayerShellGoal {
    private final int searchRadius;
    private final Class<E> targetClass;
    private final Comparator<E> comparator;
    private final Predicate<E> candidatePredicate;
    protected @Nullable E target;

    public ShellBlockGoal(PlayerShellEntity shell, Class<E> targetClass, Comparator<E> comparator, Predicate<E> candidatePredicate, int searchRadius) {
        super(shell);
        this.targetClass = targetClass;
        this.comparator = comparator;
        this.candidatePredicate = candidatePredicate;
        this.setControls(EnumSet.of(Control.LOOK, Control.MOVE));
        this.searchRadius = searchRadius;
    }

    @Override
    public boolean canStart() {
        if (!this.findInHotbar(i -> RequiemItemTags.SHIELDS.contains(i.getItem()))) {
            return false;
        }

        Box box = Box.from(this.shell.getPos()).expand(searchRadius);
        List<E> candidates = this.shell.world.getEntitiesByClass(
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

    @Override
    public void start() {
        if (this.hotbarSlot >= 0) {
            this.shell.selectHotbarSlot(this.hotbarSlot);
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
        this.shell.releaseActiveItem();
        this.target = null;
    }

    public static ShellBlockGoal<ProjectileEntity> blockProjectiles(PlayerShellEntity shell) {
        final int searchRadius = 10;

        return new ShellBlockGoal<>(
            shell,
            ProjectileEntity.class,
            Comparator.comparing(shell::squaredDistanceTo),
            candidate -> {
                // If the projectile is in the ground, it is not a threat
                if (candidate instanceof PersistentProjectileEntityAccessor
                    && ((PersistentProjectileEntityAccessor) candidate).isInGround()) return false;

                // If the attacker is close, tank it
                // (the projectile is likely to hit before the shield is up anyway)
                Entity attacker = candidate.getOwner();
                if (attacker != null && attacker.squaredDistanceTo(shell) < 9) return false;

                // raycast does not take into account the case where the projectile is already inside
                // the box, so we do it here
                Box dangerZone = shell.getBoundingBox().expand(1.5);
                if (dangerZone.intersects(candidate.getBoundingBox())) return true;

                // raycast to check if the projectile is going to hit us
                Vec3d start = candidate.getPos();
                // Fun fact, projectile rotation is completely broken so we need to use the velocity
                Vec3d end = start.add(candidate.getVelocity().normalize().multiply(searchRadius));
                return dangerZone.raycast(start, end).isPresent();
            },
            searchRadius
        );
    }

    public static ShellBlockGoal<MobEntity> blockRangedAttackers(PlayerShellEntity shell) {
        final int searchRadius = 20;

        return new ShellBlockGoal<>(
            shell,
            MobEntity.class,
            Comparator.<MobEntity, Boolean>comparing(e -> shell.getAttacker() == e).thenComparing(shell::squaredDistanceTo),
            e -> {
                // Look at mobs that want us dead
                if (e instanceof RangedAttackMob && e.getTarget() == shell) {
                    EntityNavigation nav = shell.getGuide().getNavigation();
                    // If no valid path found, keep the shield up
                    if (!nav.startMovingTo(e, 0.5) && !nav.isIdle()) {
                        return true;
                    }
                    // If close enough, tank and strike, otherwise keep the shield up
                    return shell.squaredDistanceTo(e) > 16;
                }
                return false;
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
