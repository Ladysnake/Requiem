package ladysnake.tartaros.client.proxy;

import ladysnake.tartaros.common.init.ModBlocks;
import ladysnake.tartaros.common.init.ModEntities;
import ladysnake.tartaros.common.init.ModItems;
import ladysnake.tartaros.common.proxy.CommonProxy;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit() {
		super.preInit();

		
		ModEntities.registerRenders();
	}
	
	@Override
	public void init() {
		super.init();
		ModBlocks.registerRenders();
		ModItems.registerRenders();
	}

}
