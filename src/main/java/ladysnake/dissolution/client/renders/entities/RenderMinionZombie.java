package ladysnake.tartaros.client.renders.entities;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import ladysnake.tartaros.client.model.ModelMinionZombie;
import ladysnake.tartaros.common.entity.EntityMinion;
import ladysnake.tartaros.common.entity.EntityMinionZombie;
import ladysnake.tartaros.common.entity.EntityWanderingSoul;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderMinionZombie extends RenderBiped<EntityMinionZombie> {

	private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("tartaros:textures/entity/minions/minion_zombie.png");
	private static final ResourceLocation HUSK_ZOMBIE_TEXTURES = new ResourceLocation("tartaros:textures/entity/minions/minion_husk.png");
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
    protected void preRenderCallback(EntityMinionZombie minionIn, float partialTickTime)
    {
    	
        super.preRenderCallback(minionIn, partialTickTime);
    }
}
