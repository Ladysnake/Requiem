package ladysnake.tartaros.client;

import ladysnake.tartaros.client.renders.RenderWanderingSoul;
import ladysnake.tartaros.common.entity.EntityWanderingSoul;
import ladysnake.tartaros.common.init.ModBlocks;
import ladysnake.tartaros.common.init.ModEntities;
import ladysnake.tartaros.common.init.ModItems;
import ladysnake.tartaros.common.proxy.CommonProxy;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	
	public void preInit() {
		super.preInit();

		ModEntities.registerRenders();
	}
	
	public void init() {
		super.init();
		ModBlocks.registerRenders();
		ModItems.registerRenders();
	}

}
