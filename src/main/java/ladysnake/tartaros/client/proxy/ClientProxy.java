package ladysnake.tartaros.client.proxy;

import ladysnake.tartaros.client.renders.RenderWanderingSoul;
import ladysnake.tartaros.common.capabilities.IncorporealMessage;
import ladysnake.tartaros.common.entity.EntityWanderingSoul;
import ladysnake.tartaros.common.handlers.TartarosPacketHandler;
import ladysnake.tartaros.common.init.ModBlocks;
import ladysnake.tartaros.common.init.ModEntities;
import ladysnake.tartaros.common.init.ModItems;
import ladysnake.tartaros.common.proxy.CommonProxy;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;

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
