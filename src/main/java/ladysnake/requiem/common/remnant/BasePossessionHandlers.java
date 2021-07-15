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
package ladysnake.requiem.common.remnant;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.entity.TrackingStartCallback;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.event.requiem.PossessionEvents;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessedData;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.entity.effect.AttritionStatusEffect;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.gamerule.RequiemGamerules;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.core.entity.EntityAiToggle;
import ladysnake.requiem.core.tag.RequiemCoreTags;
import ladysnake.requiem.mixin.common.access.EndermanEntityAccessor;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class BasePossessionHandlers {

    public static void register() {
        TrackingStartCallback.EVENT.register((player, tracked) -> {
            if (tracked instanceof Possessable) {
                // Synchronize possessed entities with their possessor / other players
                PlayerEntity possessor = ((Possessable) tracked).getPossessor();
                if (possessor != null) {
                    PossessionComponent.KEY.syncWith(player, ComponentProvider.fromEntity(possessor));
                }
            }
        });
        PossessionEvents.INVENTORY_TRANSFER_CHECK.register(
            (possessor, host) -> possessor.world.getGameRules().get(RequiemGamerules.POSSESSION_KEEP_INVENTORY).get().shouldTransfer(host.isAlive()) ? TriState.TRUE : TriState.DEFAULT
        );
        PossessionEvents.HOST_DEATH.register((player, host, deathCause) -> AttritionStatusEffect.apply(player));
        PossessionStartCallback.EVENT.register(Requiem.id("blacklist"), (target, possessor, simulate) -> {
            if (RequiemCoreTags.Entity.POSSESSION_BLACKLIST.contains(target.getType())) {
                return PossessionStartCallback.Result.DENY;
            }
            return PossessionStartCallback.Result.PASS;
        });
        PossessionEvents.POST_RESURRECTION.register(RequiemCriteria.PLAYER_RESURRECTED_AS_ENTITY::handle);
        PossessionStartCallback.EVENT.register(Requiem.id("base_mobs"), (target, possessor, simulate) -> {
            if (RequiemEntityTypeTags.POSSESSABLES.contains(target.getType())) {
                return PossessionStartCallback.Result.ALLOW;
            }
            return PossessionStartCallback.Result.PASS;
        });
        PossessionStartCallback.EVENT.register(Requiem.id("converted_mobs"), (target, possessor, simulate) -> {
            if (PossessedData.KEY.get(target).wasConvertedUnderPossession()) {
                return PossessionStartCallback.Result.ALLOW;
            }
            return PossessionStartCallback.Result.PASS;
        });
        PossessionStartCallback.EVENT.register(Requiem.id("emancipation"), (target, possessor, simulate) -> {
            if (possessor.hasStatusEffect(RequiemStatusEffects.EMANCIPATION)
                && EntityAiToggle.isAiDisabled(target)) {
                return PossessionStartCallback.Result.ALLOW;
            }
            return PossessionStartCallback.Result.PASS;
        });
    }

    public static void performEndermanSoulAction(MobEntity target, PlayerEntity possessor) {
        Entity tpDest;
        // Maybe consider making the dimensional teleportation work in any dimension other than the overworld ?
        if (possessor.world.getRegistryKey() != World.END/* == DimensionType.OVERWORLD*/) {
            // Retry a few times
            for (int i = 0; i < 20; i++) {
                if (((EndermanEntityAccessor) target).requiem$invokeTeleportRandomly()) {
                    possessor.world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, target.getSoundCategory(), 1.0F, 1.0F);
                    break;
                }
            }
            tpDest = target;
        } else {
            possessor.world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, target.getSoundCategory(), 1.0F, 1.0F);
            // Set the variable in advance to avoid game credits
            ((ServerPlayerEntity) possessor).notInAnyWorld = true;
            ServerWorld destination = ((ServerPlayerEntity) possessor).server.getWorld(World.OVERWORLD);
            possessor.moveToWorld(destination);
            ((ServerPlayerEntity) possessor).networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_WON, 0.0F));
            tpDest = target.moveToWorld(destination);
        }
        if (tpDest != null) {
            possessor.teleport(tpDest.getX(), tpDest.getY(), tpDest.getZ(), true);
        }
    }
}
