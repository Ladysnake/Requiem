package ladysnake.tartaros.proxy;

import ladysnake.tartaros.capabilities.IIncorporealHandler;
import ladysnake.tartaros.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.capabilities.IncorporealDataHandler.DefaultIncorporealHandler;
import ladysnake.tartaros.capabilities.IncorporealDataHandler.Storage;
import ladysnake.tartaros.handlers.EventHandlerCommon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;

public abstract class CommonProxy {
	public void preInit() {
		IncorporealDataHandler.register();
	}
	
	public void init() {
		MinecraftForge.EVENT_BUS.register(new EventHandlerCommon());
		System.out.println("init");
	}
	
	public void postInit() {
		
	}
}
