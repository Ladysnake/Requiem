package ladysnake.tartaros.common;

import ladysnake.tartaros.common.inventory.TartarosTab;
import ladysnake.tartaros.common.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.MCVERSION)
public class Tartaros {
	
	@Instance(Reference.MOD_ID)
	public static Tartaros instance;
	
	public static final CreativeTabs CREATIVE_TAB = new TartarosTab();
	
	 @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS,serverSide = Reference.SERVER_PROXY_CLASS)
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
