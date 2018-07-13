package ladysnake.dissolution.client;

import ladysnake.dissolution.client.gui.GuiIncorporealOverlay;
import ladysnake.dissolution.client.renders.tileentities.RenderWispInAJar;
import ladysnake.dissolution.common.init.CommonProxy;
import ladysnake.dissolution.common.init.ModEntities;
import ladysnake.dissolution.common.tileentities.TileEntityWispInAJar;
import ladysnake.dissolution.unused.client.models.blocks.BakedModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        ModEntities.registerRenders();
        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new GuiIncorporealOverlay(Minecraft.getMinecraft()));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWispInAJar.class, new RenderWispInAJar());

        initAddedLayers();
    }

    private static void initAddedLayers() {
        Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((s, render) -> {
//            render.addLayer(new LayerScythe());
//    			render.addLayer(new LayerDisguise(render, s.equals("slim")));
        });
    }

}
