package ladysnake.dissolution.common;


import ladysnake.dissolution.common.commands.CommandDissolutionTree;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
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
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION,
        acceptedMinecraftVersions = Reference.MCVERSION, dependencies = Reference.DEPENDENCIES,
        guiFactory = Reference.GUI_FACTORY_CLASS)
public class Dissolution {

    @Instance(Reference.MOD_ID)
    public static Dissolution instance;

    public static DissolutionConfig config = new DissolutionConfig();

    public static final CreativeTabs CREATIVE_TAB = new DissolutionTab();
    public static final Logger LOGGER = LogManager.getLogger("Dissolution");

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;
    /**True if the last server checked does not have the mod installed*/
    public static boolean noServerInstall;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
        DissolutionConfigManager.init(event.getSuggestedConfigurationFile());
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

    /**
     * This is just here to store whether the current connected server has dissolution installed, used in {@link ladysnake.dissolution.client.handlers.EventHandlerClient}
     * @see NetworkCheckHandler for signature information
     */
    @NetworkCheckHandler
    public boolean checkModLists(Map<String,String> modList, Side side) {
        boolean modInstalled = Reference.VERSION.equals(modList.get(Reference.MOD_ID));
        if (side.isServer())
            noServerInstall = !modInstalled;
        return side.isServer() || modInstalled;
    }

}
