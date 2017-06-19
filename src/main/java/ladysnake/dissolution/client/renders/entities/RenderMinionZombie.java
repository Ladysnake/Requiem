package ladysnake.dissolution.client.renders.entities;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.client.models.ModelMinionZombie;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.EntityMinionZombie;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderMinionZombie extends RenderBiped<EntityMinionZombie> {

	private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/zombie.png");
	private static final ResourceLocation HUSK_TEXTURES = new ResourceLocation("textures/entity/zombie/husk.png");
	private static final ResourceLocation ZOMBIE_MINION_TEXTURES = new ResourceLocation(Reference.MOD_ID + ":textures/entity/minions/minion_zombie.png");
	private static final ResourceLocation HUSK_MINION_TEXTURES = new ResourceLocation(Reference.MOD_ID + ":textures/entity/minions/minion_husk.png");
	private static final DataParameter<Boolean> DEATH = EntityDataManager.<Boolean>createKey(EntityMinionZombie.class, DataSerializers.BOOLEAN);
	private EntityDataManager dataManager;

    public RenderMinionZombie(RenderManager rendermanagerIn) {
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
    protected ResourceLocation getEntityTexture(@Nonnull EntityMinionZombie entity) {
    	if(entity.isCorpse())
    		return entity.isHusk() ? HUSK_TEXTURES : ZOMBIE_TEXTURES;
    	return entity.isHusk() ? HUSK_MINION_TEXTURES : ZOMBIE_MINION_TEXTURES;
    }
    
    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}
   
    @Override
    public void doRender(EntityMinionZombie minionIn, double x, double y, double z, float entityYaw, float partialTicks) {
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
    protected void preRenderCallback(EntityMinionZombie minionIn, float partialTickTime)
    {
    	if(minionIn.isHusk())
    		GlStateManager.scale(1.0625F, 1.0625F, 1.0625F);
        super.preRenderCallback(minionIn, partialTickTime);
    }
}
