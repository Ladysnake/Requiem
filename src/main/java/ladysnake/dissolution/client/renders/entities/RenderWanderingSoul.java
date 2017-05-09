package ladysnake.dissolution.client.renders.entities;

import java.util.Random;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.client.models.ModelWanderingSoul;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.EntityWanderingSoul;
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
	
	protected ResourceLocation wanderingSoulTexture = new ResourceLocation(Reference.MOD_ID + ":textures/entity/lost_soul/lostSoul_2.png");;

    public static final Factory FACTORY = new Factory();

    public RenderWanderingSoul(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelBiped(), 0.5F);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntityWanderingSoul entity) {
        return this.getSoulTexture(entity);
    }
    
    protected ResourceLocation getSoulTexture(EntityWanderingSoul soul) {
    	return new ResourceLocation(Reference.MOD_ID + ":textures/entity/lost_soul/lostsoul_"  + soul.texture_id + ".png");
    }

    public static class Factory implements IRenderFactory<EntityWanderingSoul> {

        @Override
        public Render<? super EntityWanderingSoul> createRenderFor(RenderManager manager) {
            return new RenderWanderingSoul(manager);
        }

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
}
