package ladysnake.requiemtest;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;

public class RequiemTestUtil {
    public static void startPossession(ServerPlayerEntity player, ZombieEntity zombie) {
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        RemnantComponent.get(player).setVagrant(true);
        PossessionComponent.get(player).startPossessing(zombie);
        GameTestUtil.assertTrue("Vagrant player should be able to possess zombie", PossessionComponent.get(player).getHost() == zombie);
    }

    public static ServerPlayerEntity spawnGhost(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        RemnantComponent.get(player).setVagrant(true);
        return player;
    }
}
