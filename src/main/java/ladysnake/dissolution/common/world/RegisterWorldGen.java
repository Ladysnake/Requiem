package ladysnake.dissolution.common.world;

import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RegisterWorldGen {
    public static void MainRegistry() {
        registerWorldGen(new WorldGen(), -1);
        registerWorldGen(new WorldGen(), 0);
        registerWorldGen(new WorldGen(), 1);
    }

    private static void registerWorldGen(IWorldGenerator iGenerator, int weightProbability) {
        GameRegistry.registerWorldGenerator(iGenerator, weightProbability);
    }
}