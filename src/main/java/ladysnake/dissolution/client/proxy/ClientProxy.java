package ladysnake.tartaros.client.proxy;

import ladysnake.tartaros.client.gui.GuiIncorporealOverlay;
import ladysnake.tartaros.client.handlers.EventHandlerClient;
import ladysnake.tartaros.client.renders.blocks.RenderSoulAnchor;
import ladysnake.tartaros.common.handlers.EventHandlerCommon;
import ladysnake.tartaros.common.init.ModBlocks;
import ladysnake.tartaros.common.init.ModEntities;
import ladysnake.tartaros.common.init.ModItems;
import ladysnake.tartaros.common.proxy.CommonProxy;
import ladysnake.tartaros.common.tileentities.TileEntitySoulAnchor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit() {
		super.preInit();


		ModBlocks.registerRenders();
		ModItems.registerRenders();
		ModEntities.registerRenders();
	}
	
	@Override
	public void init() {
		super.init();
		MinecraftForge.EVENT_BUS.register(new EventHandlerClient());
		MinecraftForge.EVENT_BUS.register(new GuiIncorporealOverlay(Minecraft.getMinecraft()));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySoulAnchor.class, new RenderSoulAnchor());
	}

}
