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
package ladysnake.requiem.common.entity;

import baritone.api.fakeplayer.FakePlayers;
import com.google.common.collect.ImmutableMap;
import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.registry.Registry;

public final class RequiemEntities {

    public static final EntityType<ObeliskSoulEntity> OBELISK_SOUL = FabricEntityTypeBuilder.<ObeliskSoulEntity>create()
        .entityFactory(ObeliskSoulEntity::new)
        .dimensions(EntityDimensions.changing(0.25f, 0.25f))
        .trackRangeChunks(4)
        .trackedUpdateRate(10)
        .build();
    public static final EntityType<ReleasedSoulEntity> RELEASED_SOUL = FabricEntityTypeBuilder.<ReleasedSoulEntity>create()
        .entityFactory(ReleasedSoulEntity::new)
        .dimensions(EntityDimensions.changing(0.25f, 0.25f))
        .trackRangeChunks(4)
        .trackedUpdateRate(10)
        .build();
    public static final EntityType<CuredVillagerEntity> CURED_VILLAGER = FabricEntityTypeBuilder.<CuredVillagerEntity>createMob()
        .entityFactory(CuredVillagerEntity::new)
        .dimensions(EntityType.VILLAGER.getDimensions())
        .trackRangeChunks(EntityType.VILLAGER.getMaxTrackDistance())
        .trackedUpdateRate(EntityType.VILLAGER.getTrackTickInterval())
        .defaultAttributes(VillagerEntity::createVillagerAttributes)
        .build();
    public static final EntityType<PiglinEntity> CURED_PIGLIN = FabricEntityTypeBuilder.<PiglinEntity>createMob()
        .entityFactory((entityType, world) -> {
            PiglinEntity ret = new PiglinEntity(entityType, world);
            ret.setImmuneToZombification(true);
            return ret;
        })
        .dimensions(EntityType.PIGLIN.getDimensions())
        .trackRangeChunks(EntityType.PIGLIN.getMaxTrackDistance())
        .trackedUpdateRate(EntityType.PIGLIN.getTrackTickInterval())
        .defaultAttributes(PiglinEntity::createPiglinAttributes)
        .build();
    public static final EntityType<PiglinBruteEntity> CURED_PIGLIN_BRUTE = FabricEntityTypeBuilder.<PiglinBruteEntity>createMob()
        .entityFactory((entityType, world) -> {
            PiglinBruteEntity ret = new PiglinBruteEntity(entityType, world);
            ret.setImmuneToZombification(true);
            return ret;
        })
        .dimensions(EntityType.PIGLIN_BRUTE.getDimensions())
        .trackRangeChunks(EntityType.PIGLIN_BRUTE.getMaxTrackDistance())
        .trackedUpdateRate(EntityType.PIGLIN_BRUTE.getTrackTickInterval())
        .defaultAttributes(PiglinBruteEntity::createPiglinBruteAttributes)
        .build();
    public static final ImmutableMap<EntityType<? extends MobEntity>, EntityType<? extends MobEntity>> CURED_PIGLIN_VARIANTS =
        ImmutableMap.of(EntityType.PIGLIN, CURED_PIGLIN, EntityType.PIGLIN_BRUTE, CURED_PIGLIN_BRUTE);
    public static final EntityType<PlayerEntity> PLAYER_SHELL = FabricEntityTypeBuilder.<PlayerEntity>createLiving()
        .spawnGroup(SpawnGroup.MISC)
        .entityFactory(FakePlayers.entityFactory(PlayerShellEntity::new))
        .defaultAttributes(PlayerShellEntity::createPlayerShellAttributes)
        .dimensions(EntityDimensions.changing(EntityType.PLAYER.getWidth(), EntityType.PLAYER.getHeight()))
        .trackRangeBlocks(64)
        .trackedUpdateRate(1)
        .forceTrackedVelocityUpdates(true)
        .disableSummon()
        .build();
    public static final EntityType<MorticianEntity> MORTICIAN = FabricEntityTypeBuilder.createLiving()
        .spawnGroup(SpawnGroup.CREATURE)
        .entityFactory(MorticianEntity::new)
        .defaultAttributes(MorticianEntity::createMorticianAttributes)
        .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
        .build();

    public static void init() {
        Registry.register(Registry.ENTITY_TYPE, Requiem.id("player_shell"), PLAYER_SHELL);
        Registry.register(Registry.ENTITY_TYPE, Requiem.id("obelisk_soul"), OBELISK_SOUL);
        Registry.register(Registry.ENTITY_TYPE, Requiem.id("released_soul"), RELEASED_SOUL);
        Registry.register(Registry.ENTITY_TYPE, Requiem.id("cured_villager"), CURED_VILLAGER);
        Registry.register(Registry.ENTITY_TYPE, Requiem.id("cured_piglin"), CURED_PIGLIN);
        Registry.register(Registry.ENTITY_TYPE, Requiem.id("cured_piglin_brute"), CURED_PIGLIN_BRUTE);
        Registry.register(Registry.ENTITY_TYPE, Requiem.id("mortician"), MORTICIAN);

        MorticianSpawner.init();
    }

}
