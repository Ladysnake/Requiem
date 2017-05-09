package ladysnake.dissolution.client.renders.entities;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.client.models.ModelMinionZombie;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.EntityMinionZombie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderMinionZombie extends RenderBiped<EntityMinionZombie> {

	private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation(Reference.MOD_ID + ":textures/entity/minions/minion_zombie.png");
	private static final ResourceLocation HUSK_ZOMBIE_TEXTURES = new ResourceLocation(Reference.MOD_ID + ":textures/entity/minions/minion_husk.png");
	private static final DataParameter<Boolean> DEATH = EntityDataManager.<Boolean>createKey(EntityMinionZombie.class, DataSerializers.BOOLEAN);
	private EntityDataManager dataManager;

    public static final Factory FACTORY = new Factory();

    public RenderMinionZombie(RenderManager rendermanagerIn, boolean death) {
    		super(rendermanagerIn, new ModelMinionZombie(), 0.5F);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntityMinionZombie entity) {
    	if ((entity.isHusk())) 
   			return HUSK_ZOMBIE_TEXTURES;
        return ZOMBIE_TEXTURES;
    }

    public static class Factory implements IRenderFactory<EntityMinionZombie> {

        @Override
        public Render<EntityMinionZombie> createRenderFor(RenderManager manager) {
        	
        		return new RenderMinionZombie(manager, true);	
 	
        }

    }
    
    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}
   
    @Override
    public void doRender(EntityMinionZombie minionIn, double x, double y, double z, float entityYaw, float partialTicks) {
    	GL11.glPushMatrix();
    	if(minionIn.getRemainingTicks() > 0 && minionIn.getRemainingTicks() < 1200) {
    		GL11.glColor4f(1.0f, 1.0f, 1.0f, ((float)minionIn.getRemainingTicks()) / ((float)minionIn.maxTicks));
    		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    		GL11.glEnable(GL11.GL_BLEND);
    	}
    	super.doRender(minionIn, x, y, z, entityYaw, partialTicks);
        GL11.glPopMatrix();
    }
    
    @Override
    protected void preRenderCallback(EntityMinionZombie minionIn, float partialTickTime)
    {
    	if(minionIn.isHusk())
    		GlStateManager.scale(1.0625F, 1.0625F, 1.0625F);
        super.preRenderCallback(minionIn, partialTickTime);
    }
}
