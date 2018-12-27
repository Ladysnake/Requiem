package ladysnake.dissolution.client;

import ladysnake.dissolution.client.gui.GuiIncorporealOverlay;
import ladysnake.dissolution.common.init.CommonProxy;
import ladysnake.dissolution.common.init.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        ModEntities.registerRenders();
    }

    @Override
    public void init() {
        super.init();
        MinecraftForge.EVENT_BUS.register(new GuiIncorporealOverlay(Minecraft.getMinecraft()));
    }

}
