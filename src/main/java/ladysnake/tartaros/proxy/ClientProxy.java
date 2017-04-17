package ladysnake.tartaros.proxy;

import ladysnake.tartaros.init.ModBlocks;

public class ClientProxy extends CommonProxy {
	
	public void preInit() {
		super.preInit();
	}
	
	public void init() {
		super.init();
		ModBlocks.registerRenders();
	}

}
