package ladysnake.dissolution.client.renders.entities;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import ladysnake.dissolution.client.models.entities.ModelMinionZombie;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.minion.EntityMinionZombie;
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

public class RenderMinionZombie extends RenderMinion<EntityMinionZombie> {

	private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/zombie.png");
	private static final ResourceLocation HUSK_TEXTURES = new ResourceLocation("textures/entity/zombie/husk.png");
	private static final ResourceLocation ZOMBIE_MINION_TEXTURES = new ResourceLocation(Reference.MOD_ID + ":textures/entity/minions/minion_zombie.png");
	private static final ResourceLocation HUSK_MINION_TEXTURES = new ResourceLocation(Reference.MOD_ID + ":textures/entity/minions/minion_husk.png");

    public RenderMinionZombie(RenderManager rendermanagerIn) {
    		super(rendermanagerIn, ModelZombie::new, ZOMBIE_TEXTURES, ZOMBIE_TEXTURES);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntityMinionZombie entity) {
//    	if(entity.isInert())
    		return entity.isHusk() ? HUSK_TEXTURES : ZOMBIE_TEXTURES;
//    	return entity.isHusk() ? HUSK_MINION_TEXTURES : ZOMBIE_MINION_TEXTURES;
    }
    
    @Override
    protected void preRenderCallback(EntityMinionZombie minionIn, float partialTickTime)
    {
    	if(minionIn.isHusk())
    		GlStateManager.scale(1.0625F, 1.0625F, 1.0625F);
        super.preRenderCallback(minionIn, partialTickTime);
    }
}
