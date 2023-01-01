package ladysnake.requiem.common.possession;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiemtest.RequiemTestUtil;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public class PossessionStatusEffectsTests implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void statusEffectsGetTransferred(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(0, 0, 0);
        ZombieEntity zombie = ctx.spawnMob(EntityType.ZOMBIE, 1, 0, 1);
        RequiemTestUtil.startPossession(player, zombie);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION));
        GameTestUtil.assertTrue("Possessed mob should inherit player's effects", zombie.hasStatusEffect(StatusEffects.ABSORPTION));
        player.removeStatusEffect(StatusEffects.ABSORPTION);
        GameTestUtil.assertFalse("Possessed mob should lose effect when player does", zombie.hasStatusEffect(StatusEffects.ABSORPTION));
        zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN));
        GameTestUtil.assertTrue("Player should inherit host's effects", player.hasStatusEffect(StatusEffects.BAD_OMEN));
        zombie.removeStatusEffect(StatusEffects.BAD_OMEN);
        GameTestUtil.assertFalse("Player should lose effect when host does", player.hasStatusEffect(StatusEffects.BAD_OMEN));
        ctx.complete();
    }
}
