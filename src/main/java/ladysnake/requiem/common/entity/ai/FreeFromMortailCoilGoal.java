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

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.entity.MorticianEntity;
import ladysnake.requiem.common.entity.effect.PenanceComponent;
import ladysnake.requiem.common.entity.effect.PenanceStatusEffect;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;

public class FreeFromMortailCoilGoal extends MorticianSpellGoal {
    public static final int PENANCE_TIME = 10 * 20 * 60;

    public FreeFromMortailCoilGoal(MorticianEntity mortician) {
        super(mortician);
    }

    @Override
    protected SoundEvent getSoundPrepare() {
        return RequiemSoundEvents.ENTITY_MORTICIAN_PREPARE_ATTACK;
    }

    @Override
    protected int getCooldown() {
        return 100;
    }

    @Override
    protected int getWarmupTime() {
        return 20;
    }

    @Override
    protected void castSpell() {
        LivingEntity target = this.mortician.getTarget();
        if (target instanceof ServerPlayerEntity player) {
            int penanceAmplifier = getRequiredPenance(player);
            player.addStatusEffect(new StatusEffectInstance(RequiemStatusEffects.PENANCE, PENANCE_TIME, penanceAmplifier, false, false, true));
            ServerPlayerEntity soul = PenanceComponent.KEY.get(player).maxOutPenance(penanceAmplifier);
            ServerPlayerEntity affected = soul != null ? soul : player;
            affected.takeKnockback(1, MathHelper.sin(this.mortician.getYaw() * (float) Math.PI / 180), -MathHelper.cos(this.mortician.getYaw() * (float) Math.PI / 180));
            affected.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(affected));
        }
    }

    private int getRequiredPenance(LivingEntity player) {
        if (PossessionComponent.getHost(player) != null) return PenanceStatusEffect.MOB_BAN_THRESHOLD;
        return PenanceStatusEffect.PLAYER_BAN_THRESHOLD;
    }
}
