package ladysnake.dissolution.client.renders.entities;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.common.entity.EntityMinionSquelette;
import ladysnake.dissolution.common.entity.EntityMinionStray;
import ladysnake.dissolution.client.models.ModelMinionSquelette;
import ladysnake.dissolution.client.models.ModelMinionStray;
import ladysnake.dissolution.common.entity.EntityMinion;
import ladysnake.dissolution.common.entity.EntityWanderingSoul;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerStrayClothing;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderMinionStray extends RenderBiped<EntityMinionStray> {

	private static final ResourceLocation STRAY_TEXTURES = new ResourceLocation("dissolution:textures/entity/minions/minion_stray.png");
	private static final DataParameter<Boolean> DEATH = EntityDataManager.<Boolean>createKey(EntityMinionStray.class, DataSerializers.BOOLEAN);
	private EntityDataManager dataManager;

    public static final Factory FACTORY = new Factory();

    public RenderMinionStray(RenderManager rendermanagerIn, boolean death) {
    		super(rendermanagerIn, new ModelMinionStray(), 0.5F);
    		this.addLayer(new LayerMinionStrayClothing(this));
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntityMinionStray entity) {
    		return STRAY_TEXTURES;

    }

    public static class Factory implements IRenderFactory<EntityMinionStray> {

        @Override
        public Render<EntityMinionStray> createRenderFor(RenderManager manager) {
        	
        		return new RenderMinionStray(manager, true);	
 	
        }

    }
   
    
    @Override
    protected void preRenderCallback(EntityMinionStray minionIn, float partialTickTime)
    {
    	
        super.preRenderCallback(minionIn, partialTickTime);
    }
}
