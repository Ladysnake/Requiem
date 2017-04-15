package ladysnake.tartaros;

import ladysnake.tartaros.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.MCVERSION)
public class Tartaros {
	
	@Instance("Tartaros")
	public Tartaros instance;
	
	 @SidedProxy(clientSide = "ladysnake.tartaros.proxy.ClientProxy",serverSide = "ladysnake.tartaros.proxy.ServerProxy")
	 public static CommonProxy proxy;
	 
	 @EventHandler
	 public void preInit(FMLPreInitializationEvent event) {
		 proxy.preInit();
	 }
	 
	 @EventHandler
	 public void init(FMLInitializationEvent event) {
		 proxy.init();
	 }
	 
	 @EventHandler
	 public void postInit(FMLPostInitializationEvent e) {
		 proxy.postInit();
	 }
}
