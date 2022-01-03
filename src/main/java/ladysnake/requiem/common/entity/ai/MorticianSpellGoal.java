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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.sound.SoundEvent;

public abstract class MorticianSpellGoal extends Goal {
    protected final MorticianEntity mortician;
    protected int spellCooldown;
    protected int nextCastTime;

    public MorticianSpellGoal(MorticianEntity mortician) {
        this.mortician = mortician;
    }

    @Override
    public void start() {
        this.spellCooldown = this.getWarmupTime();
        this.nextCastTime = this.mortician.age + this.getCooldown();
        this.mortician.playSound(this.getSoundPrepare(), 1.0F, 1.0F);
        this.mortician.setSpellcasting(true);
    }

    @Override
    public boolean canStart() {
        LivingEntity target = this.mortician.getTarget();
        if (target != null && target.isAlive()) {
            if (this.mortician.isSpellcasting()) {
                return false;
            } else {
                return this.mortician.age >= this.nextCastTime;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = this.mortician.getTarget();
        return target != null && target.isAlive() && this.spellCooldown > 0;
    }

    protected abstract SoundEvent getSoundPrepare();

    @Override
    public void stop() {
        this.mortician.setSpellcasting(false);
    }

    @Override
    public void tick() {
        --this.spellCooldown;
        if (this.spellCooldown == 0) {
            this.castSpell();
            this.mortician.playSound(this.mortician.getCastSpellSound(), 1.0F, 1.0F);
            this.mortician.stopAnger();
        }
    }

    protected abstract int getCooldown();

    protected abstract int getWarmupTime();

    protected abstract void castSpell();
}
