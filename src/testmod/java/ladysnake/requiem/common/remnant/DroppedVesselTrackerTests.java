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
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.common.gamerule.RequiemGamerules;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

public class DroppedVesselTrackerTests implements FabricGameTest {
    public static final String DROP_VESSEL_ON_DISCONNECT_BATCH = "dropVesselOnDisconnect";

    @BeforeBatch(batchId = DROP_VESSEL_ON_DISCONNECT_BATCH)
    public void setDropVesselOnDisconnect(ServerWorld world) {
        world.getGameRules().get(RequiemGamerules.DROP_VESSEL_ON_DISCONNECT).set(true, world.getServer());
    }

    @AfterBatch(batchId = DROP_VESSEL_ON_DISCONNECT_BATCH)
    public void resetDropVesselOnDisconnect(ServerWorld world) {
        world.getGameRules().get(RequiemGamerules.DROP_VESSEL_ON_DISCONNECT).set(false, world.getServer());
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = DROP_VESSEL_ON_DISCONNECT_BATCH)
    public void remnantPlayersLeaveShellBehindWithGameruleEnabled(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 0, 2);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        player.networkHandler.onDisconnected(Text.empty());
        ctx.expectEntity(RequiemEntities.PLAYER_SHELL);
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void remnantPlayersDoNotLeaveHostBehindByDefault(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 0, 2);
        MobEntity host = ctx.spawnEntity(EntityType.ZOMBIE, 3, 0, 3);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        RemnantComponent.get(player).setVagrant(true);
        PossessionComponent.get(player).startPossessing(host);
        player.networkHandler.onDisconnected(Text.empty());
        GameTestUtil.assertTrue("Host should be removed alongside possessor", host.isRemoved());
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = DROP_VESSEL_ON_DISCONNECT_BATCH)
    public void remnantPlayersLeaveHostBehindWithGameruleEnabled(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 0, 2);
        MobEntity host = ctx.spawnEntity(EntityType.ZOMBIE, 3, 0, 3);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        RemnantComponent.get(player).setVagrant(true);
        PossessionComponent.get(player).startPossessing(host);
        player.networkHandler.onDisconnected(Text.empty());
        GameTestUtil.assertFalse("Host should not be removed alongside possessor", host.isRemoved());
        ctx.complete();
    }
}
