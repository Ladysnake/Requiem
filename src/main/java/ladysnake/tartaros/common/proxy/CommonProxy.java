package ladysnake.tartaros.common.proxy;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler.DefaultIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler.Storage;
import ladysnake.tartaros.common.handlers.EventHandlerCommon;
import ladysnake.tartaros.common.handlers.TartarosPacketHandler;
import ladysnake.tartaros.common.init.ModBlocks;
import ladysnake.tartaros.common.init.ModEntities;
import ladysnake.tartaros.common.init.ModItems;
import ladysnake.tartaros.common.inventory.GuiProxy;
import ladysnake.tartaros.common.networkingtest.PacketHandler;
import ladysnake.tartaros.common.tileentities.TileEntityCrystallizer;
import ladysnake.tartaros.common.tileentities.TileEntitySoulExtractor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class CommonProxy {
	public void preInit() {
		IncorporealDataHandler.register();
		ModBlocks.init();
		ModBlocks.register();
		ModItems.init();
		ModItems.register();
		ModEntities.register();
	}
	
	public void init() {
		MinecraftForge.EVENT_BUS.register(new EventHandlerCommon());
		System.out.println("init");
		
		GameRegistry.registerTileEntity(TileEntityCrystallizer.class, Reference.MOD_ID + "tileentitycrystallizer");
		GameRegistry.registerTileEntity(TileEntitySoulExtractor.class, Reference.MOD_ID + "tileentitysoulextractor");
		NetworkRegistry.INSTANCE.registerGuiHandler(Tartaros.instance, new GuiProxy());
		PacketHandler.initPackets();
		TartarosPacketHandler.initPackets();
	}
	
	public void postInit() {
		
	}
}
