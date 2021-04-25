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
package ladysnake.pandemonium.common.entity.effect;

import ladysnake.pandemonium.Pandemonium;
import ladysnake.pandemonium.common.PlayerSplitter;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.api.v1.remnant.StickyStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PenanceStatusEffect extends StatusEffect {
    protected PenanceStatusEffect(StatusEffectType type, int color) {
        super(type, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (amplifier >= 1) {
            PossessionComponent c = PossessionComponent.KEY.getNullable(entity);
            if (c != null) { // It's 1 because amplifiers are 0 based for some fucking reason
                c.stopPossessing();
            }
            if (entity instanceof ServerPlayerEntity && !(RemnantComponent.get((PlayerEntity) entity)).isVagrant()) {
                if (RemnantComponent.get((PlayerEntity) entity).getRemnantType().isDemon()) {
                    PlayerSplitter.split((ServerPlayerEntity) entity);
                } else {
                    entity.damage(DamageSource.MAGIC, amplifier*4);
                }
            }
        }
    }

    public static PossessionStartCallback.@NotNull Result canPossess(MobEntity target, PlayerEntity possessor, boolean simulate) {
        StatusEffectInstance penance = possessor.getStatusEffect(PandemoniumStatusEffects.PENANCE);
        if (penance != null && penance.getAmplifier() >= 3 && target instanceof PlayerShellEntity) {
            return PossessionStartCallback.Result.DENY;
        }
        return PossessionStartCallback.Result.PASS;
    }
}
