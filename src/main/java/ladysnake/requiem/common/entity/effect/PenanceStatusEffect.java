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

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.event.requiem.CanCurePossessedCallback;
import ladysnake.requiem.api.v1.event.requiem.PlayerShellEvents;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.PlayerSplitResult;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.StickyStatusEffect;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class PenanceStatusEffect extends StatusEffect implements StickyStatusEffect {

    public static final int PREVENT_CURE_THRESHOLD = 0;
    public static final int PLAYER_BAN_THRESHOLD = 1;
    public static final int MOB_BAN_THRESHOLD = 2;

    public PenanceStatusEffect(StatusEffectCategory type, int color) {
        super(type, color);
    }

    public static void registerCallbacks() {
        CanCurePossessedCallback.EVENT.register((body) -> {
            if (body.hasStatusEffect(RequiemStatusEffects.PENANCE)) {
                return TriState.FALSE;
            }
            return TriState.DEFAULT;
        });
        PlayerShellEvents.PRE_MERGE.register(PenanceStatusEffect::canMerge);
        PossessionStartCallback.EVENT.register(Requiem.id("deny_penance_three"), (target, possessor, simulate) ->
            getLevel(possessor) >= MOB_BAN_THRESHOLD ? PossessionStartCallback.Result.DENY : PossessionStartCallback.Result.PASS);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (PenanceComponent.KEY.maybeGet(entity).filter(PenanceComponent::shouldApplyPenance).isPresent()) {
            applyPenance(entity, amplifier);
        }
    }

    @Override
    public boolean shouldStick(LivingEntity entity) {
        return RemnantComponent.KEY.maybeGet(entity).map(rc -> rc.getRemnantType().isDemon()).orElse(false);
    }

    @Override
    public boolean shouldFreezeDuration(LivingEntity entity) {
        return false;
    }

    /**
     *
     * @param entity the entity getting affected with penance
     * @param amplifier {@link StatusEffectInstance#getAmplifier()}
     * @return the respawned player if the target got split, otherwise {@code null}
     */
    public static Result applyPenance(LivingEntity entity, int amplifier) {
        if (amplifier >= PLAYER_BAN_THRESHOLD && entity instanceof ServerPlayerEntity player) { // level 2+
            if (amplifier >= MOB_BAN_THRESHOLD) { // level 3+
                PossessionComponent possessionComponent = PossessionComponent.get(player);
                if (possessionComponent.isPossessionOngoing()) {
                    possessionComponent.stopPossessing();
                    return new Result(true, null);
                }
            }
            RemnantComponent remnant = RemnantComponent.get(player);
            if (!remnant.isVagrant()) {
                if (remnant.getRemnantType().isDemon()) {
                    return new Result(true, remnant.splitPlayer(true).map(PlayerSplitResult::soul).orElse(null));
                } else {
                    player.damage(DamageSource.MAGIC, (amplifier + 1) * 4);
                    PenanceComponent.KEY.get(player).resetPenanceTime();
                }
            }
        }

        return new Result(false, null);
    }

    public static int getLevel(PlayerEntity player) {
        StatusEffectInstance penance = player.getStatusEffect(RequiemStatusEffects.PENANCE);
        return penance == null ? -1 : penance.getAmplifier();
    }

    public static boolean canMerge(PlayerEntity possessor, PlayerEntity target, GameProfile shellProfile) {
        StatusEffectInstance penance = possessor.getStatusEffect(RequiemStatusEffects.PENANCE);
        return penance == null || penance.getAmplifier() < PLAYER_BAN_THRESHOLD;
    }

    public record Result(boolean split, @Nullable ServerPlayerEntity soul) {

    }
}
