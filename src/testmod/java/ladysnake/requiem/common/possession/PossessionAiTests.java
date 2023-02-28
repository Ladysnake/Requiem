package ladysnake.requiem.common.possession;

import ladysnake.requiemtest.RequiemTestUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import org.quiltmc.qsl.testing.api.game.QuiltGameTest;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;

public class PossessionAiTests implements QuiltGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void possessedMobsCantTargetThemselves(QuiltTestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        ZombieEntity mu = ctx.spawnMob(EntityType.ZOMBIE, 2, 0, 1);
        ZombieEntity nu = ctx.spawnMob(EntityType.ZOMBIE, 3, 0, 1);
        ZombieEntity xi = ctx.spawnMob(EntityType.ZOMBIE, 4, 0, 1);
        RequiemTestUtil.startPossession(player, mu);
        ctx.waitAndRun(1, () -> {
            mu.tryAttack(nu);
            ctx.succeedWhen(() -> {
                ctx.assertTrue(nu.getTarget() == mu, "Attacked party should target attacker");
                ctx.assertTrue(xi.getTarget() == mu, "Third party angerables should target attacker");
                ctx.assertTrue(mu.getTarget() == null, "Attacker should not target itself");
            });
        });
    }
}
