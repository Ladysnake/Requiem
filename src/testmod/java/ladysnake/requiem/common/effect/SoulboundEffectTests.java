package ladysnake.requiem.common.effect;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiemtest.RequiemTestUtil;
import ladysnake.requiemtest.mixin.EffectCommandAccessor;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

public class SoulboundEffectTests implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void attritionSticksToSpirits(TestContext ctx) {
        ServerPlayerEntity ghost = RequiemTestUtil.spawnGhost(ctx);
        ghost.addStatusEffect(new StatusEffectInstance(RequiemStatusEffects.ATTRITION, 30, 0));
        ghost.clearStatusEffects();
        ctx.runAtTick(1, () -> ctx.addInstantFinalTask(() -> GameTestUtil.assertTrue("Player should keep attrition", ghost.hasStatusEffect(RequiemStatusEffects.ATTRITION))));
    }

    @GameTest(structureName = EMPTY_STRUCTURE, duration = 2)
    public void clearCommandBypassesSticky(TestContext ctx) {
        ServerPlayerEntity ghost = RequiemTestUtil.spawnGhost(ctx);
        ghost.addStatusEffect(new StatusEffectInstance(RequiemStatusEffects.ATTRITION, 30, 0));
        EffectCommandAccessor.invokeExecuteClear(ghost.getCommandSource(), List.of(ghost));
        ctx.runAtTick(1, () -> ctx.addInstantFinalTask(() -> GameTestUtil.assertFalse("Player should lose attrition", ghost.hasStatusEffect(RequiemStatusEffects.ATTRITION))));
    }
}
