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
package ladysnake.requiem.common.entity.effect;

import ladysnake.requiem.api.v1.event.requiem.PlayerShellEvents;
import ladysnake.requiem.api.v1.event.requiem.PossessionEvents;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.StickyStatusEffect;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

import java.util.Map;
import java.util.WeakHashMap;

public class ReclamationStatusEffect extends StatusEffect implements StickyStatusEffect {
    private static final Map<LivingEntity, Integer> playersToHeal = new WeakHashMap<>();

    public ReclamationStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    public static void registerEventHandlers() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            playersToHeal.forEach(AttritionStatusEffect::reduce);
            playersToHeal.clear();
        });
        PossessionEvents.DISSOCIATION_CLEANUP.register(ReclamationStatusEffect::clearReclamation);
        PlayerShellEvents.PLAYER_SPLIT.register((whole, soul, playerShell) -> clearReclamation(soul, whole));
    }

    private static void clearReclamation(ServerPlayerEntity soul, LivingEntity body) {
        if (body.hasStatusEffect(RequiemStatusEffects.RECLAMATION)) {
            body.removeStatusEffect(RequiemStatusEffects.RECLAMATION);
            soul.playSound(RequiemSoundEvents.EFFECT_RECLAMATION_CLEAR, SoundCategory.PLAYERS, 1, 0.8f);
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration == 1;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.world.isClient()) {
            playersToHeal.put(entity, amplifier + 1);
        }
    }

    @Override
    public boolean shouldStick(LivingEntity entity) {
        return false;
    }

    @Override
    public boolean shouldFreezeDuration(LivingEntity entity) {
        return RemnantComponent.isIncorporeal(entity);
    }
}
