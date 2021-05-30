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
package ladysnake.requiem.common.impl.remnant;

import com.google.common.base.Preconditions;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.CurableEntityComponent;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.event.requiem.CanCurePossessedCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.StickyStatusEffect;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.entity.effect.AttritionStatusEffect;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.impl.movement.SerializableMovementConfig;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class MutableRemnantState implements RemnantState {

    public static final AbilitySource SOUL_STATE = Pal.getAbilitySource(Requiem.id("soul_state"));
    protected final PlayerEntity player;
    private boolean ethereal;

    public MutableRemnantState(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void setup(RemnantState oldHandler) {
        this.setVagrant(oldHandler.isVagrant());
    }

    @Override
    public void teardown(RemnantState newHandler) {
        this.updatePlayerState(false);
    }

    @Override
    public boolean isIncorporeal() {
        return this.isVagrant() && !PossessionComponent.get(this.player).isPossessing();
    }

    @Override
    public boolean isVagrant() {
        return this.ethereal;
    }

    @Override
    public boolean setVagrant(boolean vagrant) {
        this.ethereal = vagrant;
        this.updatePlayerState(vagrant);
        return true;
    }

    private void updatePlayerState(boolean vagrant) {
        SerializableMovementConfig config;
        boolean serverside = !this.player.world.isClient;
        if (vagrant) {
            config = SerializableMovementConfig.SOUL;
            if (serverside) {
                Pal.grantAbility(this.player, VanillaAbilities.INVULNERABLE, SOUL_STATE);
            }
        } else {
            config = null;
            if (serverside) {
                Pal.revokeAbility(this.player, VanillaAbilities.INVULNERABLE, SOUL_STATE);
            }
        }
        MovementAlterer.get(player).setConfig(config);
        RemnantComponent.KEY.sync(player);
    }

    @Override
    public boolean canDissociateFrom(MobEntity possessed) {
        return RequiemEntityTypeTags.FRICTIONLESS_HOSTS.contains(possessed.getType());
    }

    @Override
    public void curePossessed(LivingEntity body) {
        if (!this.canCurePossessed(body)) {
            return;
        }
        if (this.canRegenerateBodyFrom(body)) {
            ServerPlayerEntity player = (ServerPlayerEntity) this.player;
            RequiemNetworking.sendBodyCureMessage(player);
            this.regenerateBody(body);
        } else {
            this.cureMob(body);
        }
        AttritionStatusEffect.reduce(this.player, Integer.MAX_VALUE);
    }

    private void regenerateBody(LivingEntity body) {
        Preconditions.checkState(!this.player.world.isClient);
        RemnantComponent.get(player).setVagrant(false);
        RequiemCriteria.TRANSFORMED_POSSESSED_ENTITY.handle((ServerPlayerEntity) player, body, player, true);
        body.remove(Entity.RemovalReason.DISCARDED);
        player.removeStatusEffect(RequiemStatusEffects.ATTRITION);
        player.setHealth(body.getHealth());
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
        player.world.syncWorldEvent(null, 1027, player.getBlockPos(), 0);
    }

    protected void cureMob(LivingEntity body) {
        MobEntity cured = CurableEntityComponent.KEY.get(body).cure();

        if (cured != null) {
            PossessionComponent.get(this.player).startPossessing(cured);
            RequiemCriteria.TRANSFORMED_POSSESSED_ENTITY.handle((ServerPlayerEntity) this.player, body, cured, true);
        }
    }

    @Override
    public boolean canCurePossessed(LivingEntity body) {
        return CanCurePossessedCallback.EVENT.invoker().canCurePossessed(body).get();
    }

    @Override
    public boolean canRegenerateBody() {
        return true;
    }

    protected boolean canRegenerateBodyFrom(LivingEntity body) {
        return this.canRegenerateBody() && CurableEntityComponent.KEY.get(body).canBeAssimilated();
    }

    @Override
    public void prepareRespawn(ServerPlayerEntity original, boolean lossless) {
        if (!lossless && !this.isVagrant()) {
            RemnantComponent.get(this.player).setVagrant(true);
            this.copyGlobalPos(original);

            if (original.isDead()) {
                AttritionStatusEffect.apply(player);
            }
        }
        for (StatusEffectInstance effect : original.getStatusEffects()) {
            if (StickyStatusEffect.shouldStick(effect.getEffectType(), this.player)) {
                this.player.addStatusEffect(new StatusEffectInstance(effect));
            }
        }
    }

    protected void copyGlobalPos(ServerPlayerEntity original) {
        ServerPlayerEntity clone = (ServerPlayerEntity) this.player;
        ServerWorld previousWorld = original.getServerWorld();
        clone.setWorld(previousWorld);
        clone.interactionManager.setWorld(previousWorld);
        clone.copyPositionAndRotation(original);
    }

    @Override
    public void serverTick() {
        // NO-OP
    }
}
