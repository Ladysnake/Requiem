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
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import ladysnake.dissolution.common.tileentities.TileEntityPowerCore;
import ladysnake.dissolution.common.tileentities.TileEntitySepulture;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class CommonProxy {
	
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(ModBlocks.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ModItems.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ModModularSetups.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ModFluids.REGISTRY_MANAGER);
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
		
		GameRegistry.registerTileEntity(TileEntityPowerCore.class, Reference.MOD_ID + "tileentitypowercore");
		GameRegistry.registerTileEntity(TileEntitySepulture.class, Reference.MOD_ID + "tileentitysepulture");
		GameRegistry.registerTileEntity(TileEntityModularMachine.class, Reference.MOD_ID + "tileentitymodularmachine");

		NetworkRegistry.INSTANCE.registerGuiHandler(Dissolution.instance, new GuiProxy());
		PacketHandler.initPackets();
	}
	
	public void postInit() {}
	
	public void spawnParticle(World world, float x, float y, float z, float vx, float vy, float vz, float r, float g, float b, float a, float scale, int lifetime) {}
}
