package ladysnake.dissolution.client.renders.entities;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.client.models.ModelMinionSkeleton;
import ladysnake.dissolution.client.models.ModelMinionStray;
import ladysnake.dissolution.client.models.ModelMinionWitherSkeleton;
import ladysnake.dissolution.common.entity.EntityWanderingSoul;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.entity.minion.EntityMinionSkeleton;
import ladysnake.dissolution.common.entity.minion.EntityMinionStray;
import ladysnake.dissolution.common.entity.minion.EntityMinionWitherSkeleton;
import ladysnake.dissolution.common.entity.minion.EntityMinionZombie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerStrayClothing;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderMinionWitherSkeleton extends RenderBiped<EntityMinionWitherSkeleton> {

	private static final ResourceLocation STRAY_TEXTURES = new ResourceLocation("dissolution:textures/entity/minions/minion_wither_skeleton.png");
	private static final DataParameter<Boolean> DEATH = EntityDataManager.<Boolean>createKey(EntityMinionWitherSkeleton.class, DataSerializers.BOOLEAN);
	private EntityDataManager dataManager;

    public static final Factory FACTORY = new Factory();

    public RenderMinionWitherSkeleton(RenderManager rendermanagerIn, boolean death) {
    		super(rendermanagerIn, new ModelMinionWitherSkeleton(), 0.5F);
    		LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this)
            {
                protected void initArmor()
                {
                    this.modelLeggings = new ModelMinionWitherSkeleton(0.5F, true);
                    this.modelArmor = new ModelMinionWitherSkeleton(1.0F, true);
                }
            };
            this.addLayer(layerbipedarmor);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntityMinionWitherSkeleton entity) {
    		return STRAY_TEXTURES;

    }

    public static class Factory implements IRenderFactory<EntityMinionWitherSkeleton> {

        @Override
        public Render<EntityMinionWitherSkeleton> createRenderFor(RenderManager manager) {
        	
        		return new RenderMinionWitherSkeleton(manager, true);	
 	
        }

    }
   
    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}
   
    @Override
    public void doRender(EntityMinionWitherSkeleton minionIn, double x, double y, double z, float entityYaw, float partialTicks) {
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
    
    @Override
    protected void preRenderCallback(EntityMinionWitherSkeleton minionIn, float partialTickTime)
    {
    	GlStateManager.scale(1.2F, 1.2F, 1.2F);
        super.preRenderCallback(minionIn, partialTickTime);
    }
}
