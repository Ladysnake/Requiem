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
