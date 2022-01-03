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
package ladysnake.requiem.common.possession;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.mixin.common.access.EntityAccessor;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DummyGoatJumpingMount implements ExternalJumpingMount, TransientComponent {
    public static final ComponentKey<DummyGoatJumpingMount> KEY = ComponentRegistry.getOrCreate(Requiem.id("charged_jump"), DummyGoatJumpingMount.class);
    private final GoatEntity goat;
    private float jumpStrength;
    /**@see HorseBaseEntity#isInAir()*/
    private boolean inAir;

    public DummyGoatJumpingMount(GoatEntity goat) {
        this.goat = goat;
    }

    @Override
    public void setJumpStrength(int strength) {
        if (strength < 0) {
            strength = 0;
        } else {
            PlayerEntity possessor = ((Possessable) this.goat).getPossessor();
            if (possessor != null) {
                possessor.setJumping(true);
            }
        }

        if (strength >= 90) {
            this.jumpStrength = 1.0F;
        } else {
            this.jumpStrength = 0.4F + 0.4F * (float)strength / 90.0F;
        }
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void startJumping(int height) {
        this.goat.setNoDrag(true);
        this.goat.setPose(EntityPose.LONG_JUMPING);
    }

    @Override
    public void stopJumping() {
        this.goat.world.playSoundFromEntity(null, this.goat, SoundEvents.ENTITY_GOAT_STEP, SoundCategory.NEUTRAL, 2.0F, 1.0F);
        this.goat.setNoDrag(false);
        this.goat.setPose(EntityPose.STANDING);
    }

    @Override
    public void attemptJump() {
        PlayerEntity possessor = ((Possessable) this.goat).getPossessor();
        if (possessor != null && this.jumpStrength > 0.0F && !this.inAir && possessor.isOnGround()) {
            double naturalStrength = 1.0;
            double baseJumpVelocity = naturalStrength * this.jumpStrength * ((EntityAccessor) this.goat).requiem$invokeGetJumpVelocityMultiplier();
            double jumpVelocity = baseJumpVelocity + this.goat.getJumpBoostVelocityModifier();
            Vec3d baseVelocity = possessor.getVelocity();
            possessor.setVelocity(baseVelocity.x, jumpVelocity, baseVelocity.z);
            this.inAir = true;
            possessor.velocityDirty = true;
            if (possessor.forwardSpeed > 0.0F) {
                float vx = MathHelper.sin(possessor.getYaw() * (float) (Math.PI / 180.0));
                float vz = MathHelper.cos(possessor.getYaw() * (float) (Math.PI / 180.0));
                possessor.setVelocity(possessor.getVelocity().add(-0.4F * vx * this.jumpStrength, 0.0, 0.4F * vz * this.jumpStrength));
            }

            this.jumpStrength = 0.0F;
        }
    }

    @Override
    public void endJump() {
        this.jumpStrength = 0.0F;
        this.inAir = false;
    }
}
