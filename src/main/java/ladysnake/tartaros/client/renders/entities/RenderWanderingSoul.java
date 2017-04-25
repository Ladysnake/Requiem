package ladysnake.tartaros.client.renders.entities;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import ladysnake.tartaros.client.models.ModelWanderingSoul;
import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.entity.EntityWanderingSoul;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderWanderingSoul extends RenderLiving<EntityWanderingSoul> {
	
	protected ResourceLocation wanderingSoulTexture;

	public RenderWanderingSoul(RenderManager renderManager, ModelBase modelBase, float shadowSize) {
		super(renderManager, modelBase, shadowSize);
		setEntityTexture();
	}

	private void setEntityTexture() {
		Random rand = new Random();
		switch(rand.nextInt()){
		case 1: wanderingSoulTexture = new ResourceLocation(Reference.MOD_ID + ":textures/entity/lost_soul/lostSoul_2.png"); break;
		case 2: wanderingSoulTexture = new ResourceLocation(Reference.MOD_ID + ":textures/entity/lost_soul/lostSoul_3.png"); break;
		case 3: wanderingSoulTexture = new ResourceLocation(Reference.MOD_ID + ":textures/entity/lost_soul/lostSoul_4.png"); break;
		default: wanderingSoulTexture = new ResourceLocation(Reference.MOD_ID + ":textures/entity/lost_soul/lostSoul_1.png"); break;
		}
		
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWanderingSoul entity) {
		return wanderingSoulTexture;
	}
	
	@Override
	public void doRender(EntityWanderingSoul entity, double x, double y, double z, float entityYaw, float partialTicks){
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}
	
	@Override
	public void setLightmap(EntityWanderingSoul entityLivingIn, float partialTicks) {
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
	}
	
	
	public static class Factory implements IRenderFactory<EntityWanderingSoul> {

        @Override
        public Render<? super EntityWanderingSoul> createRenderFor(RenderManager manager) {
            return new RenderWanderingSoul(manager, new ModelBiped(1.0f), 0.5f);
        }

}
	
}
