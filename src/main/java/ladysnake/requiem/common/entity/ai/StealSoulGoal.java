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
package ladysnake.requiem.common.entity.ai;

import ladysnake.requiem.common.entity.MorticianEntity;
import ladysnake.requiem.common.item.EmptySoulVesselItem;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.core.entity.SoulHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;

import java.util.Objects;

public class StealSoulGoal extends MorticianSpellGoal {
    public StealSoulGoal(MorticianEntity mortician) {
        super(mortician);
    }

    @Override
    public boolean canStart() {
        if (super.canStart()) {
            return this.mortician.getTarget() != null && EmptySoulVesselItem.canAttemptCapture(this.mortician, this.mortician.getTarget());
        }
        return false;
    }

    @Override
    protected SoundEvent getSoundPrepare() {
        return RequiemSoundEvents.ENTITY_MORTICIAN_PREPARE_ATTACK;
    }

    @Override
    protected int getCooldown() {
        return 200;
    }

    @Override
    protected int getWarmupTime() {
        return 100;
    }

    @Override
    public void tick() {
        EmptySoulVesselItem.playSoulCaptureEffects(this.mortician, Objects.requireNonNull(this.mortician.getTarget()));
        super.tick();
    }

    @Override
    protected void castSpell() {
        LivingEntity target = this.mortician.getTarget();
        if (target != null && EmptySoulVesselItem.canAttemptCapture(this.mortician, target)) {
            if (EmptySoulVesselItem.wins(this.mortician, target)) {
                this.mortician.addCapturedSoul(EmptySoulVesselItem.setupRecord(target));
                SoulHolderComponent.get(target).removeSoul();
            } else {
                for(int i = 0; i < 7; i++) {
                    double vx = this.mortician.getRandom().nextGaussian() * 0.02;
                    double vy = this.mortician.getRandom().nextGaussian() * 0.02;
                    double vz = this.mortician.getRandom().nextGaussian() * 0.02;
                    this.mortician.world.addParticle(
                        ParticleTypes.SMOKE,
                        this.mortician.getParticleX(1.0),
                        this.mortician.getRandomBodyY() + 0.5,
                        this.mortician.getParticleZ(1.0),
                        vx,
                        vy,
                        vz
                    );
                }
            }
        }
    }
}
