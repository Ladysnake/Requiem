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
package ladysnake.requiem.common.possession.jump;

import com.google.common.base.Preconditions;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import ladysnake.requiem.api.v1.entity.ExternalJumpingMount;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.mixin.common.access.EntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * A fake JumpingMount that allows players possessing a mob to long jump by themselves
 */
public class DummyJumpingMount implements ExternalJumpingMount, TransientComponent {
    protected final LivingEntity mob;
    private final SoundEvent stepSound;
    private float jumpStrength;
    /**@see HorseBaseEntity#isInAir()*/
    private boolean inAir;
    private final double baseJumpStrength;

    public DummyJumpingMount(LivingEntity mob, double baseJumpStrength, SoundEvent stepSound) {
        this.mob = mob;
        this.baseJumpStrength = baseJumpStrength;
        this.stepSound = stepSound;
    }

    @Override
    public void setJumpStrength(int strength) {
        if (strength < 0) {
            strength = 0;
        } else {
            PlayerEntity possessor = ((Possessable) this.mob).getPossessor();
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
        this.mob.setPose(EntityPose.LONG_JUMPING);
    }

    @Override
    public void stopJumping() {
        this.mob.world.playSoundFromEntity(null, this.mob, this.stepSound, SoundCategory.NEUTRAL, 2.0F, 1.0F);
        this.mob.setPose(EntityPose.STANDING);
    }

    @Override
    public void attemptJump() {
        PlayerEntity possessor = ((Possessable) this.mob).getPossessor();

        if (possessor != null && possessor.isOnGround()) {
            if (!this.inAir && this.jumpStrength > 0.0F) {
                double naturalStrength = getBaseJumpingStrength();
                double baseJumpVelocity = naturalStrength * this.jumpStrength * ((EntityAccessor) this.mob).requiem$invokeGetJumpVelocityMultiplier();
                double jumpVelocity = baseJumpVelocity + this.mob.getJumpBoostVelocityModifier();
                Vec3d baseVelocity = possessor.getVelocity();
                possessor.setVelocity(baseVelocity.x, jumpVelocity, baseVelocity.z);
                this.inAir = true;
                possessor.velocityDirty = true;
                if (possessor.forwardSpeed > 0.0F) {
                    float vx = MathHelper.sin(possessor.getYaw() * (float) (Math.PI / 180.0));
                    float vz = MathHelper.cos(possessor.getYaw() * (float) (Math.PI / 180.0));
                    possessor.setVelocity(possessor.getVelocity().add(-0.4F * vx * this.jumpStrength, 0.0, 0.4F * vz * this.jumpStrength));
                }

                this.beginClientJump(possessor);
            } else if (this.inAir) {
                this.finishClientJump(possessor);
            }
        }
    }

    protected double getBaseJumpingStrength() {
        return baseJumpStrength;
    }

    protected void beginClientJump(PlayerEntity possessor) {
        this.mob.setPose(EntityPose.LONG_JUMPING);
        this.jumpStrength = 0.0F;
    }

    protected void finishClientJump(PlayerEntity possessor) {
        Preconditions.checkState(this.mob.world.isClient, "endJump should only be called clientside");

        this.jumpStrength = 0.0F;
        this.inAir = false;

        // Apparently this packet never gets sent under normal conditions in vanilla
        MinecraftClient.getInstance().player.networkHandler.sendPacket(new ClientCommandC2SPacket(this.mob, ClientCommandC2SPacket.Mode.STOP_RIDING_JUMP));
    }
}
