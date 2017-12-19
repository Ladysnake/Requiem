package ladysnake.dissolution.common.init;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.OreDictHelper;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityDistillateHandler;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import ladysnake.dissolution.common.handlers.EventHandlerCommon;
import ladysnake.dissolution.common.handlers.InteractEventsHandler;
import ladysnake.dissolution.common.handlers.LivingDeathHandler;
import ladysnake.dissolution.common.handlers.PlayerTickHandler;
import ladysnake.dissolution.common.inventory.GuiProxy;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.tileentities.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public abstract class CommonProxy {

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(ModBlocks.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ModItems.INSTANCE);
        CapabilityIncorporealHandler.register();
        CapabilitySoulHandler.register();
        CapabilityDistillateHandler.register();
        CapabilityGenericInventoryProvider.register();
        ModStructure.init();
    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(new EventHandlerCommon());
        MinecraftForge.EVENT_BUS.register(new LivingDeathHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerTickHandler());
        MinecraftForge.EVENT_BUS.register(new InteractEventsHandler());

        OreDictHelper.registerOres();

        GameRegistry.registerTileEntity(TileEntitySepulture.class, Reference.MOD_ID + ":tileentitysepulture");
        GameRegistry.registerTileEntity(TileEntityLamentStone.class, Reference.MOD_ID + ":tileentityancienttomb");
        GameRegistry.registerTileEntity(TileEntityWispInAJar.class, Reference.MOD_ID + ":tileentitywispinajar");

        NetworkRegistry.INSTANCE.registerGuiHandler(Dissolution.instance, new GuiProxy());
        PacketHandler.initPackets();
    }

    public void postInit() {
    }

    public void spawnParticle(World world, float x, float y, float z, float vx, float vy, float vz, int r, int g, int b, int a, float scale, int lifetime) {
    }

    public abstract Side getSide();
}
