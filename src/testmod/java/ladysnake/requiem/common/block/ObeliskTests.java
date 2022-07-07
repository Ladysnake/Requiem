package ladysnake.requiem.common.block;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.common.block.obelisk.InertRunestoneBlock;
import ladysnake.requiem.common.block.obelisk.RunestoneBlockEntity;
import ladysnake.requiemtest.mixin.WorldAccessor;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class ObeliskTests implements FabricGameTest {
    public static final String SMALL_OBELISK_STRUCTURE = "requiem-test:small_obelisk";

    @GameTest(structureName = SMALL_OBELISK_STRUCTURE)
    public void obelisksGetDetected(TestContext ctx) {
        BlockPos controllerPos = new BlockPos(2, 3, 2);
        BlockPos absoluteControllerPos = ctx.getAbsolutePos(controllerPos);
        InertRunestoneBlock.tryActivateObelisk(ctx.getWorld(), absoluteControllerPos, true);
        ctx.waitAndRun(2, () -> {
            BlockState controllerState = ctx.getBlockState(controllerPos);
            GameTestUtil.assertTrue("Bottommost runestone block should be activated", controllerState.get(InertRunestoneBlock.ACTIVATED));
            GameTestUtil.assertTrue("Topmost runestone block should be activated", ctx.getBlockState(controllerPos.up()).get(InertRunestoneBlock.ACTIVATED));
            GameTestUtil.assertTrue("Bottommost runestone block should be marked as controller", controllerState.get(InertRunestoneBlock.HEAD));
            GameTestUtil.assertTrue("Topmost runestone block should have a block entity", ctx.getBlockEntity(controllerPos.up()) instanceof RunestoneBlockEntity);
            GameTestUtil.assertTrue("Bottommost runestone block should have a block entity", ctx.getBlockEntity(controllerPos) instanceof RunestoneBlockEntity);
            GameTestUtil.assertTrue("Controller block entity should be ticking", ((WorldAccessor) ctx.getWorld()).getBlockEntityTickers().stream().anyMatch(i -> absoluteControllerPos.equals(i.getPos())));
            GameTestUtil.assertFalse("Non-controller block entity should not be ticking", ((WorldAccessor) ctx.getWorld()).getBlockEntityTickers().stream().anyMatch(i -> absoluteControllerPos.up().equals(i.getPos())));
            ctx.complete();
        });
    }
}
