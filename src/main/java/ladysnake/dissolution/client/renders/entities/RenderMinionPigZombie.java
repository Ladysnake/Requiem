package ladysnake.dissolution.client.renders.entities;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.client.models.ModelMinionZombie;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.EntityMinionPigZombie;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderMinionPigZombie extends RenderBiped<EntityMinionPigZombie> {
	
	private static final ResourceLocation ZOMBIE_PIGMAN_TEXTURE = new ResourceLocation("textures/entity/zombie_pigman.png");
	private static final ResourceLocation ZOMBIE_PIGMAN_MINION_TEXTURES = new ResourceLocation(Reference.MOD_ID + ":textures/entity/minions/minion_zombie_pigman.png");

	public static final Factory FACTORY = new Factory();

    public RenderMinionPigZombie(RenderManager rendermanagerIn, boolean death) {
    		super(rendermanagerIn, new ModelMinionZombie(), 0.5F);
    		LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this)
            {
                protected void initArmor()
                {
                    this.modelLeggings = new ModelMinionZombie(0.5F, true);
                    this.modelArmor = new ModelMinionZombie(1.0F, true);
                }
            };
            this.addLayer(layerbipedarmor);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntityMinionPigZombie entity) {
    	if(entity.isCorpse())
    		return ZOMBIE_PIGMAN_TEXTURE;
    	return ZOMBIE_PIGMAN_MINION_TEXTURES;
    }

    public static class Factory implements IRenderFactory<EntityMinionPigZombie> {

        @Override
        public Render<EntityMinionPigZombie> createRenderFor(RenderManager manager) {
        	
        		return new RenderMinionPigZombie(manager, true);	
 	
        }

    }
    
    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}
   
    @Override
    public void doRender(EntityMinionPigZombie minionIn, double x, double y, double z, float entityYaw, float partialTicks) {
    	GL11.glPushMatrix();
    	if(minionIn.isCorpse() && minionIn.getRemainingTicks() > 0 && minionIn.getRemainingTicks() < minionIn.MAX_DEAD_TICKS) {
    		GL11.glColor4f(1.0f, 1.0f, 1.0f, ((float)minionIn.getRemainingTicks()) / ((float)minionIn.MAX_DEAD_TICKS));
    		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    		GL11.glEnable(GL11.GL_BLEND);
    	} else if (minionIn.getRemainingTicks() > 0 && minionIn.getRemainingTicks() < minionIn.MAX_RISEN_TICKS) {
        	float colorRatio = ((float)minionIn.getRemainingTicks()) / ((float)minionIn.MAX_RISEN_TICKS);
    		GL11.glColor4f(colorRatio, colorRatio, colorRatio, 1.0f);
    	}
    	super.doRender(minionIn, x, y, z, entityYaw, partialTicks);
        GL11.glPopMatrix();
    }
}
