package ladysnake.tartaros.proxy;

import ladysnake.tartaros.handlers.EventHandlerCommon;
import net.minecraftforge.common.MinecraftForge;

public abstract class CommonProxy {
	public void preInit() {
		
	}
	
	public void init() {
		MinecraftForge.EVENT_BUS.register(new EventHandlerCommon());
		System.out.println("init");
	}
	
	public void postInit() {
		
	}
}
