package ladysnake.tartaros.common.proxy;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.handlers.EventHandlerCommon;
import ladysnake.tartaros.common.init.ModBlocks;
import ladysnake.tartaros.common.init.ModCrafting;
import ladysnake.tartaros.common.init.ModEntities;
import ladysnake.tartaros.common.init.ModItems;
import ladysnake.tartaros.common.inventory.GuiProxy;
import ladysnake.tartaros.common.inventory.TartarosTab;
import ladysnake.tartaros.common.networking.PacketHandler;
import ladysnake.tartaros.common.tileentities.TileEntityCrystallizer;
import ladysnake.tartaros.common.tileentities.TileEntitySepulture;
import ladysnake.tartaros.common.tileentities.TileEntitySoulAnchor;
import ladysnake.tartaros.common.tileentities.TileEntitySoulExtractor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
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
		GameRegistry.registerTileEntity(TileEntitySepulture.class, Reference.MOD_ID + "tileentitysepulture");
		GameRegistry.registerTileEntity(TileEntitySoulAnchor.class, Reference.MOD_ID + "tileentitysoulanchor");
		NetworkRegistry.INSTANCE.registerGuiHandler(Tartaros.instance, new GuiProxy());
		PacketHandler.initPackets();
		ModCrafting.register();
	}
	
	public void postInit() {
		
	}
}
