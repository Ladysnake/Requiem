package ladysnake.dissolution.common;


import ladylib.LLibContainer;
import ladylib.LadyLib;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.commands.CommandDissolutionTree;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.handlers.EventHandlerCommon;
import ladysnake.dissolution.common.handlers.InteractEventsHandler;
import ladysnake.dissolution.common.handlers.PlayerRespawnHandler;
import ladysnake.dissolution.common.handlers.PlayerTickHandler;
import ladysnake.dissolution.common.init.CommonProxy;
import ladysnake.dissolution.common.inventory.DissolutionTab;
import ladysnake.dissolution.common.inventory.GuiProxy;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(modid = Ref.MOD_ID, name = Ref.MOD_NAME, version = Ref.VERSION,
        acceptedMinecraftVersions = Ref.MCVERSION, dependencies = Ref.DEPENDENCIES,
        guiFactory = Ref.GUI_FACTORY_CLASS)
public class Dissolution {

    @Instance(Ref.MOD_ID)
    public static Dissolution instance;

    public static DissolutionConfig config = new DissolutionConfig();

    public static final CreativeTabs CREATIVE_TAB = new DissolutionTab();
    public static final Logger LOGGER = LogManager.getLogger("Dissolution");

    @SidedProxy(clientSide = Ref.CLIENT_PROXY_CLASS, serverSide = Ref.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;
    /**True if the last server checked does not have the mod installed*/
    public static boolean noServerInstall;

    @LadyLib.LLInstance
    private static LLibContainer lib;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CapabilityIncorporealHandler.register();

        DissolutionConfigManager.init(event.getSuggestedConfigurationFile());

        lib.setCreativeTab(CREATIVE_TAB);
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EventHandlerCommon());
        MinecraftForge.EVENT_BUS.register(new PlayerRespawnHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerTickHandler());
        MinecraftForge.EVENT_BUS.register(new InteractEventsHandler());

        OreDictHelper.registerOres();

        LootTableList.register(new ResourceLocation(Ref.MOD_ID, "inject/human"));

        NetworkRegistry.INSTANCE.registerGuiHandler(Dissolution.instance, new GuiProxy());
        PacketHandler.initPackets();

        proxy.init();
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
        boolean modInstalled = Ref.VERSION.equals(modList.get(Ref.MOD_ID));
        if (side.isServer()) {
            noServerInstall = !modInstalled;
        }
        return side.isServer() || modInstalled;
    }

}
