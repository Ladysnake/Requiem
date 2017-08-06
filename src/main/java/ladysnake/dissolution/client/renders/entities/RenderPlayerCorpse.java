package ladysnake.dissolution.client.renders.entities;

import com.mojang.authlib.GameProfile;

import ladysnake.dissolution.client.models.ModelMinionZombie;
import ladysnake.dissolution.client.models.ModelPlayerCorpse;
import ladysnake.dissolution.client.renders.ShaderHelper;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;

public class RenderPlayerCorpse extends RenderBiped<EntityPlayerCorpse> {
	
	private ResourceLocation texture;
	private boolean shouldRenderName = false;
		
	public RenderPlayerCorpse(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelPlayerCorpse(0.0F, true), 0.5F);
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
	protected ResourceLocation getEntityTexture(EntityPlayerCorpse entity) {
		try {
			if(texture == null)
				texture = new EntityOtherPlayerMP(entity.world, new GameProfile(entity.getPlayer(), "")).getLocationSkin();
			return texture;
		} catch (IllegalArgumentException e) {
			return DefaultPlayerSkin.getDefaultSkinLegacy();
		}
	}
	
	@Override
	protected void preRenderCallback(EntityPlayerCorpse entitylivingbaseIn, float partialTickTime) {}
	
	@Override
	public void doRender(EntityPlayerCorpse entity, double x, double y, double z, float entityYaw, float partialTicks) {
		ShaderHelper.useShader(ShaderHelper.dissolution);
		ShaderHelper.setUniform("texture", 0);
		ShaderHelper.setUniform("lightmap", 1);
		//System.out.println(Math.abs(entity.getRemainingTicks() / (float) entity.getMaxTimeRemaining()));
		ShaderHelper.setUniform("animationProgress", entity.isDecaying() ? 1 - (entity.getRemainingTicks() / (float) entity.getMaxTimeRemaining()) : 0);
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		float light = Math.max(entity.world.getLightFor(EnumSkyBlock.SKY, entity.getPosition()) * entity.world.getSunBrightnessFactor(1.0f), 
				entity.world.getLightFor(EnumSkyBlock.BLOCK, entity.getPosition()));
		ShaderHelper.setUniform("lighting", light);
		this.shouldRenderName = false;
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		ShaderHelper.revert();
		this.shouldRenderName = true;
		this.renderName(entity, x, y, z);
	}
	
	@Override
	protected boolean canRenderName(EntityPlayerCorpse entity) {
		return super.canRenderName(entity) && this.shouldRenderName;
	}

}
