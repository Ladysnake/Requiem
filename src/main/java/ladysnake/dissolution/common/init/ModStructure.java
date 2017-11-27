package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.world.WorldGen;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModStructure {

    public static void init() {
        WorldGen gen = new WorldGen();
        GameRegistry.registerWorldGenerator(gen, 0);
    }

}
