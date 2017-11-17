package ladysnake.dissolution.client.proxy;

import ladysnake.dissolution.client.gui.GuiIncorporealOverlay;
import ladysnake.dissolution.client.handlers.AlbedoEventHandler;
import ladysnake.dissolution.client.models.blocks.BakedModelLoader;
import ladysnake.dissolution.client.particles.AdditiveParticle;
import ladysnake.dissolution.client.particles.DissolutionParticleManager;
import ladysnake.dissolution.client.renders.entities.LayerScythe;
import ladysnake.dissolution.client.renders.tileentities.TileEntityCrucibleRenderer;
import ladysnake.dissolution.client.renders.tileentities.TileEntityMortarRenderer;
import ladysnake.dissolution.common.init.CommonProxy;
import ladysnake.dissolution.common.init.ModEntities;
import ladysnake.dissolution.common.tileentities.TileEntityCrucible;
import ladysnake.dissolution.common.tileentities.TileEntityMortar;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	
	private int particleCount = 0;
	
	@Override
	public void preInit() {
		super.preInit();
		ModEntities.registerRenders();
		ModelLoaderRegistry.registerLoader(new BakedModelLoader());
	}
	
	@Override
	public void init() {
		super.init();
//		PacketHandler.initClientPackets();
		MinecraftForge.EVENT_BUS.register(new GuiIncorporealOverlay(Minecraft.getMinecraft()));
		if(Loader.isModLoaded("albedo"))
			MinecraftForge.EVENT_BUS.register(AlbedoEventHandler.class);
		// ClientRegistry.bindTileEntitySpecialRenderer(TileEntityModularMachine.class, new RenderModularMachine());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrucible.class, new TileEntityCrucibleRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMortar.class, new TileEntityMortarRenderer());

		initAddedLayers();
	}

	@Override
	public void postInit() {
		super.postInit();
	}

	private static void initAddedLayers() {
    	Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((s, render) -> {
    			render.addLayer(new LayerScythe());
//    			render.addLayer(new LayerDisguise(render, s.equals("slim")));
    		});
    }
    
    @Override
    public void spawnParticle(World world, float x, float y, float z, float vx, float vy, float vz, int r, int g,
    		int b, int a, float scale, int lifetime) {
    	particleCount += world.rand.nextInt(3);
		if (particleCount % (Minecraft.getMinecraft().gameSettings.particleSetting == 0 ? 1 : 2*Minecraft.getMinecraft().gameSettings.particleSetting) == 0){
			DissolutionParticleManager.INSTANCE.addParticle(
					new AdditiveParticle(world,x,y,z, scale, lifetime, true)
					.setColor(new Color(r, g, b, a))
					.setMotion(vx, vy, vz));
		}
    }

	@Override
	public Side getSide() {
		return Side.CLIENT;
	}

}
