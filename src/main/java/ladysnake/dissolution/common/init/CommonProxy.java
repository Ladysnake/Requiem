package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import ladysnake.dissolution.common.handlers.EventHandlerCommon;
import ladysnake.dissolution.common.handlers.InteractEventsHandler;
import ladysnake.dissolution.common.handlers.LivingDeathHandler;
import ladysnake.dissolution.common.handlers.PlayerTickHandler;
import ladysnake.dissolution.common.inventory.GuiProxy;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.tileentities.TileEntityCrystallizer;
import ladysnake.dissolution.common.tileentities.TileEntityResuscitator;
import ladysnake.dissolution.common.tileentities.TileEntitySepulture;
import ladysnake.dissolution.common.tileentities.TileEntitySoulAnchor;
import ladysnake.dissolution.common.tileentities.TileEntitySoulCandle;
import ladysnake.dissolution.common.tileentities.TileEntitySoulExtractor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class CommonProxy {
	
	public void preInit() {
		ModBlocks.INSTANCE.init();
		ModItems.INSTANCE.init();
		MinecraftForge.EVENT_BUS.register(ModBlocks.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ModItems.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ModFluids.REGISTRY_MANAGER);
		MinecraftForge.EVENT_BUS.register(ModSounds.REGISTRY_MANAGER);
		CapabilityIncorporealHandler.register();
		CapabilitySoulHandler.register();
		ModEntities.register();
		ModStructure.init();
	}
	
	public void init() {
		MinecraftForge.EVENT_BUS.register(new EventHandlerCommon());
		MinecraftForge.EVENT_BUS.register(new LivingDeathHandler());
		MinecraftForge.EVENT_BUS.register(new PlayerTickHandler());
		MinecraftForge.EVENT_BUS.register(new InteractEventsHandler());
		
		ModItems.INSTANCE.registerOres();
		
		GameRegistry.registerTileEntity(TileEntityCrystallizer.class, Reference.MOD_ID + "tileentitycrystallizer");
		GameRegistry.registerTileEntity(TileEntitySoulExtractor.class, Reference.MOD_ID + "tileentitysoulextractor");
		GameRegistry.registerTileEntity(TileEntitySepulture.class, Reference.MOD_ID + "tileentitysepulture");
		GameRegistry.registerTileEntity(TileEntitySoulAnchor.class, Reference.MOD_ID + "tileentitysoulanchor");
		GameRegistry.registerTileEntity(TileEntitySoulCandle.class, Reference.MOD_ID + "tileentitysoulcandle");

		NetworkRegistry.INSTANCE.registerGuiHandler(Dissolution.instance, new GuiProxy());
		PacketHandler.initPackets();
	}
	
	public void postInit() {
		
	}
}
