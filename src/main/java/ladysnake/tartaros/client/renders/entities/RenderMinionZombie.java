package ladysnake.tartaros.client.renders.entities;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import ladysnake.tartaros.common.entity.EntityMinion;
import ladysnake.tartaros.common.entity.EntityMinionZombie;
import ladysnake.tartaros.common.entity.EntityWanderingSoul;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderMinionZombie extends RenderBiped<EntityMinionZombie> {

	private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/zombie.png");
	private static final ResourceLocation HUSK_ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/husk.png");

    public static final Factory FACTORY = new Factory();

    public RenderMinionZombie(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelZombie(), 0.5F);
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
            return new RenderMinionZombie(manager);
        }

    }
    
    @Override
    protected void preRenderCallback(EntityMinionZombie minionIn, float partialTickTime)
    {
    	if (minionIn.isHusk()) {
	        float f = 1.0625F;
	        GlStateManager.scale(1.0625F, 1.0625F, 1.0625F);
    	}
        super.preRenderCallback(minionIn, partialTickTime);
    }
}
