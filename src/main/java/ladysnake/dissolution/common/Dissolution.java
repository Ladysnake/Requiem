package ladysnake.dissolution.common;


import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.init.CommonProxy;
import ladysnake.dissolution.common.inventory.DissolutionTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.MCVERSION)
public class Dissolution {
	
	@Instance(Reference.MOD_ID)
	public static Dissolution instance;
	
	public static final CreativeTabs CREATIVE_TAB = new DissolutionTab();
	
	 @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS,serverSide = Reference.SERVER_PROXY_CLASS)
	 public static CommonProxy proxy;
	 
	 @EventHandler
	 public void preInit(FMLPreInitializationEvent event) {
		 DissolutionConfigManager.loadConfig(event.getSuggestedConfigurationFile());
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
