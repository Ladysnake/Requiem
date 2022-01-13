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
package ladysnake.requiem.common;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import ladysnake.requiem.api.v1.entity.CurableEntityComponent;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.internal.StatusEffectReapplicator;
import ladysnake.requiem.api.v1.possession.PossessedData;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.api.v1.remnant.AttritionFocus;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.client.ClientRecordKeeper;
import ladysnake.requiem.common.entity.CoolPlayerMovementAlterer;
import ladysnake.requiem.common.entity.SkeletonBoneComponent;
import ladysnake.requiem.common.entity.WololoComponent;
import ladysnake.requiem.common.entity.cure.CurableZombifiedPiglinComponent;
import ladysnake.requiem.common.entity.cure.DelegatingCurableEntityComponent;
import ladysnake.requiem.common.entity.cure.SimpleCurableEntityComponent;
import ladysnake.requiem.common.entity.effect.PenanceComponent;
import ladysnake.requiem.common.entity.effect.StatusEffectReapplicatorImpl;
import ladysnake.requiem.common.gamerule.RequiemSyncedGamerules;
import ladysnake.requiem.common.possession.DummyGoatJumpingMount;
import ladysnake.requiem.common.possession.LootingPossessedData;
import ladysnake.requiem.common.remnant.GlobalAttritionFocus;
import ladysnake.requiem.common.remnant.PlayerBodyTracker;
import ladysnake.requiem.common.remnant.RemnantComponentImpl;
import ladysnake.requiem.common.remnant.SimpleAttritionFocus;
import ladysnake.requiem.core.ability.ImmutableMobAbilityController;
import ladysnake.requiem.core.ability.PlayerAbilityController;
import ladysnake.requiem.core.entity.EntityAiToggle;
import ladysnake.requiem.core.entity.SoulHolderComponent;
import ladysnake.requiem.core.possession.PossessionComponentImpl;
import ladysnake.requiem.core.record.EntityPositionClerk;
import ladysnake.requiem.core.record.ServerRecordKeeper;
import ladysnake.requiem.core.remnant.RevivingDeathSuspender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.GoatEntity;

public final class RequiemComponents implements EntityComponentInitializer, ScoreboardComponentInitializer {

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // the order here is important, possession must be synced/deserialized after remnant
        registry.registerForPlayers(RemnantComponent.KEY, RemnantComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(PossessionComponent.KEY, PossessionComponentImpl::new, RespawnCopyStrategy.INVENTORY);
        // order does not matter for the other components
        registry.registerForPlayers(MovementAlterer.KEY, CoolPlayerMovementAlterer::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerForPlayers(DeathSuspender.KEY, RevivingDeathSuspender::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerFor(EndermanEntity.class, WololoComponent.KEY, WololoComponent::create);
        registry.registerFor(MobEntity.class, PossessedData.KEY, LootingPossessedData::new);
        registry.registerFor(MobEntity.class, MobAbilityController.KEY, e -> new ImmutableMobAbilityController<>());
        registry.registerForPlayers(MobAbilityController.KEY, player -> new PlayerAbilityController(player, VanillaRequiemPlugin.SOUL_ABILITY_CONFIG), RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerFor(MobEntity.class, SkeletonBoneComponent.KEY, SkeletonBoneComponent::new);
        registry.registerFor(MobEntity.class, AttritionFocus.KEY, p -> new SimpleAttritionFocus());
        registry.registerForPlayers(StatusEffectReapplicator.KEY, StatusEffectReapplicatorImpl::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerFor(MobEntity.class, CurableEntityComponent.KEY, SimpleCurableEntityComponent::new);
        registry.registerFor(ZombieVillagerEntity.class, CurableEntityComponent.KEY, DelegatingCurableEntityComponent::new);
        registry.registerFor(ZombifiedPiglinEntity.class, CurableEntityComponent.KEY, CurableZombifiedPiglinComponent::new);
        registry.registerFor(LivingEntity.class, EntityAiToggle.KEY, EntityAiToggle::new);
        registry.registerFor(LivingEntity.class, EntityPositionClerk.KEY, EntityPositionClerk::new);
        registry.registerForPlayers(PlayerBodyTracker.KEY, PlayerBodyTracker::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(PenanceComponent.KEY, PenanceComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerFor(LivingEntity.class, SoulHolderComponent.KEY, SoulHolderComponent::new);
        registry.registerFor(GoatEntity.class, DummyGoatJumpingMount.KEY, DummyGoatJumpingMount::new);
    }

    @Override
    public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(AttritionFocus.KEY, GlobalAttritionFocus.class, (sc, server) -> new GlobalAttritionFocus(server));
        registry.registerScoreboardComponent(RequiemSyncedGamerules.KEY, (scoreboard, server) -> new RequiemSyncedGamerules(server));
        registry.registerScoreboardComponent(GlobalRecordKeeper.KEY, (scoreboard, server) -> server == null
            ? new ClientRecordKeeper(scoreboard)
            : new ServerRecordKeeper(scoreboard, server)
        );
    }
}
