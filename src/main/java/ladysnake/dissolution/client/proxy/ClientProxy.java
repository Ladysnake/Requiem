package ladysnake.dissolution.client.proxy;

import ladysnake.dissolution.client.gui.GuiIncorporealOverlay;
import ladysnake.dissolution.client.models.blocks.BakedModelLoader;
import ladysnake.dissolution.client.particles.AdditiveParticle;
import ladysnake.dissolution.client.particles.DissolutionParticleManager;
import ladysnake.dissolution.client.renders.entities.LayerDisguise;
import ladysnake.dissolution.client.renders.entities.LayerScythe;
import ladysnake.dissolution.common.init.CommonProxy;
import ladysnake.dissolution.common.init.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;

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
		MinecraftForge.EVENT_BUS.register(new GuiIncorporealOverlay(Minecraft.getMinecraft()));
		// ClientRegistry.bindTileEntitySpecialRenderer(TileEntityModularMachine.class, new RenderModularMachine());
		initAddedLayers();
	}
	
    private static void initAddedLayers() {
    	Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((s, render) -> {
    			render.addLayer(new LayerScythe());
    			render.addLayer(new LayerDisguise(render, s.equals("slim")));
    		});
    }
    
    @Override
    public void spawnParticle(World world, float x, float y, float z, float vx, float vy, float vz, float r, float g,
    		float b, float a, float scale, int lifetime) {
    	particleCount += world.rand.nextInt(3);
		if (particleCount % (Minecraft.getMinecraft().gameSettings.particleSetting == 0 ? 1 : 2*Minecraft.getMinecraft().gameSettings.particleSetting) == 0){
			DissolutionParticleManager.INSTANCE.addParticle(new AdditiveParticle(world,x,y,z,vx,vy,vz,r,g,b,a, scale, lifetime));
}
    }

}
