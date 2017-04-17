package ladysnake.tartaros.client.renders;

import org.lwjgl.opengl.GL11;

import ladysnake.tartaros.client.models.ModelWanderingSoul;
import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.entity.EntityWanderingSoul;
import net.minecraft.client.model.ModelBase;
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
		wanderingSoulTexture = new ResourceLocation(Reference.MOD_ID + ":textures/entity/wanderingsoul/lost_soul_1.png");
		
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
            return new RenderWanderingSoul(manager, new ModelWanderingSoul(1.0f, false), 0.5f);
        }

}
	
}
