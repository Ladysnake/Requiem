package ladysnake.dissolution.common;


import ladysnake.dissolution.common.commands.CommandDissolutionTree;
import ladysnake.dissolution.common.init.CommonProxy;
import ladysnake.dissolution.common.inventory.DissolutionTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
		acceptedMinecraftVersions = Reference.MCVERSION, dependencies = Reference.DEPENDENCIES)
public class Dissolution {
	
	@Instance(Reference.MOD_ID)
	public static Dissolution instance;
	
	public static final CreativeTabs CREATIVE_TAB = new DissolutionTab();
	
	 @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS,serverSide = Reference.SERVER_PROXY_CLASS)
	 public static CommonProxy proxy;
	 
	 @EventHandler
	 public void preInit(FMLPreInitializationEvent event) {
		 proxy.preInit();
		 DissolutionConfigManager.init();
	 }
	 
	 @EventHandler
	 public void init(FMLInitializationEvent event) {
		 proxy.init();
	 }
	 
	 @EventHandler
	 public void postInit(FMLPostInitializationEvent event) {
		 proxy.postInit();
	 }

	 @EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
	 	event.registerServerCommand(new CommandDissolutionTree());
	 }
}
