package ladysnake.dissolution.common.init;

import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(ModItems.INSTANCE);
    }

}
