/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.block.Blocks;
import net.minecraft.block.WeightedPressurePlateBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;
import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class VagrantPlayerTests implements QuiltGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void vagrantPlayersDoNotTriggerPressurePlates(TestContext ctx) {
        ServerPlayerEntity regularPlayer = ctx.spawnServerPlayer(2, 0, 2);
        RemnantComponent.get(regularPlayer).become(RemnantTypes.MORTAL);
        ServerPlayerEntity vagrantPlayer = ctx.spawnServerPlayer(2, 0, 4);
        RemnantComponent.get(vagrantPlayer).become(RemnantTypes.REMNANT);
        RemnantComponent.get(vagrantPlayer).setVagrant(true);
        BlockPos regularPlatePos = new BlockPos(2, 0, 2);
        BlockPos vagrantPlatePos = new BlockPos(2, 0, 4);
        ctx.setBlockState(regularPlatePos, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        ctx.setBlockState(vagrantPlatePos, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        ctx.getBlockState(regularPlatePos).onEntityCollision(ctx.getWorld(), ctx.getAbsolutePos(regularPlatePos), regularPlayer);
        ctx.getBlockState(vagrantPlatePos).onEntityCollision(ctx.getWorld(), ctx.getAbsolutePos(vagrantPlatePos), vagrantPlayer);
        ctx.succeedIf(() -> {
            ctx.expectBlockProperty(regularPlatePos, WeightedPressurePlateBlock.POWER, 1);
            ctx.expectBlockProperty(vagrantPlatePos, WeightedPressurePlateBlock.POWER, 0);
        });
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void vagrantPlayersDoNotTriggerSculkSensors(TestContext ctx) {
        ServerPlayerEntity regularPlayer = ctx.spawnServerPlayer(2, 0, 2);
        RemnantComponent.get(regularPlayer).become(RemnantTypes.MORTAL);
        ServerPlayerEntity vagrantPlayer = ctx.spawnServerPlayer(2, 0, 4);
        RemnantComponent.get(vagrantPlayer).become(RemnantTypes.REMNANT);
        RemnantComponent.get(vagrantPlayer).setVagrant(true);
        ServerPlayerEntity possessor = ctx.spawnServerPlayer(2, 0, 6);
        RemnantComponent.get(possessor).become(RemnantTypes.REMNANT);
        RemnantComponent.get(possessor).setVagrant(true);
        MobEntity zombie = ctx.spawnMob(EntityType.HUSK, 2, 0, 6);
        PossessionComponent.get(possessor).startPossessing(zombie);

        var listener = new GameEventListener() {
            private final Map<LivingEntity, Set<GameEvent>> detectedEntities = new WeakHashMap<>();

            public boolean detected(LivingEntity entity, GameEvent event) {
                return detectedEntities.getOrDefault(entity, Set.of()).contains(event);
            }

            @Override
            public PositionSource getPositionSource() {
                return new BlockPositionSource(ctx.getAbsolutePos(new BlockPos(4, 0, 4)));
            }

            @Override
            public int getRange() {
                return 3;
            }

            @Override
            public boolean listen(ServerWorld world, GameEvent gameEvent, GameEvent.Context context, Vec3d vec3d) {
                if (context.sourceEntity() instanceof LivingEntity living) {
                    this.detectedEntities.computeIfAbsent(living, e -> new HashSet<>()).add(gameEvent);
                    return true;
                }
                return false;
            }
        };
        ctx.getWorld().getChunk(regularPlayer.getBlockPos()).m_fbrqbtve(ChunkSectionPos.getSectionCoord(regularPlayer.getY())).m_zgxgxcnm(listener);
        Vec3d movement = new Vec3d(2, 0, 0);
        regularPlayer.move(MovementType.SELF, movement);
        vagrantPlayer.move(MovementType.SELF, movement);
        possessor.move(MovementType.SELF, movement);
        ctx.getWorld().emitGameEvent(regularPlayer, GameEvent.TELEPORT, regularPlayer.getPos());
        ctx.getWorld().emitGameEvent(vagrantPlayer, GameEvent.TELEPORT, regularPlayer.getPos());
        ctx.getWorld().emitGameEvent(possessor, GameEvent.TELEPORT, regularPlayer.getPos());
        ctx.waitAndRun(1, () -> ctx.succeedIf(() -> {
            GameTestUtil.assertTrue("Mortal players should send step game events", listener.detected(regularPlayer, GameEvent.STEP));
            GameTestUtil.assertTrue("Mortal players should send other game events", listener.detected(regularPlayer, GameEvent.TELEPORT));
            GameTestUtil.assertFalse("Vagrant players should not send step game events", listener.detected(vagrantPlayer, GameEvent.STEP));
            GameTestUtil.assertTrue("Vagrant players should send other game events", listener.detected(vagrantPlayer, GameEvent.TELEPORT));
            GameTestUtil.assertFalse("Possessing players should not send step game events", listener.detected(possessor, GameEvent.STEP));
            GameTestUtil.assertFalse("Possessing players should not send other game events", listener.detected(possessor, GameEvent.TELEPORT));
            GameTestUtil.assertTrue("Possessed mobs should send step game events", listener.detected(zombie, GameEvent.STEP));
            GameTestUtil.assertTrue("Possessed mobs should send other game events", listener.detected(zombie, GameEvent.TELEPORT));
            ctx.complete();
        }));
    }
}
