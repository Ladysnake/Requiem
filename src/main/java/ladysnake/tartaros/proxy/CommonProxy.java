package ladysnake.tartaros.proxy;

import ladysnake.tartaros.Reference;
import ladysnake.tartaros.Tartaros;
import ladysnake.tartaros.capabilities.IIncorporealHandler;
import ladysnake.tartaros.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.capabilities.IncorporealDataHandler.DefaultIncorporealHandler;
import ladysnake.tartaros.capabilities.IncorporealDataHandler.Storage;
import ladysnake.tartaros.handlers.EventHandlerCommon;
import ladysnake.tartaros.init.ModBlocks;
import ladysnake.tartaros.inventory.GuiProxy;
import ladysnake.tartaros.tileentities.TileEntityCrystallizer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class CommonProxy {
	public void preInit() {
		IncorporealDataHandler.register();
		ModBlocks.init();
		ModBlocks.register();
	}
	
	public void init() {
		MinecraftForge.EVENT_BUS.register(new EventHandlerCommon());
		System.out.println("init");
		
		GameRegistry.registerTileEntity(TileEntityCrystallizer.class, Reference.MOD_ID + "tileentitycrystallizer");
		NetworkRegistry.INSTANCE.registerGuiHandler(Tartaros.instance, new GuiProxy());
	}
	
	public void postInit() {
		
	}
}
