package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.client.renders.ShaderHelper;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.souls.AbstractSoul;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.io.IOException;

public class RenderWillOWisp<T extends Entity> extends Render<T> {

	public static ResourceLocation WILL_O_WISP_TEXTURE = new ResourceLocation(Reference.MOD_ID, "entity/will_o_wisp");

	public static void init() {
		try {
			ShaderLinkHelper.setNewStaticShaderLinkHelper();
			ShaderGroup shaderGroup = new ShaderGroup(
					Minecraft.getMinecraft().getTextureManager(),
					Minecraft.getMinecraft().getResourceManager(),
					Minecraft.getMinecraft().getFramebuffer(),
					new ResourceLocation("shaders/post/bloom.json"));
			shaderGroup.createBindFramebuffers(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RenderWillOWisp(RenderManager renderManager) {
		super(renderManager);
		this.shadowOpaque = 0;
	}

	@Override
	public void doRender(@Nonnull T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (!this.renderOutlines)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate((float)x, (float)y, (float)z);

			TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
			TextureAtlasSprite textureatlassprite = texturemap.getAtlasSprite(WILL_O_WISP_TEXTURE.toString());

			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
//			GlStateManager.scale(0.5F, 0.5F, 0.5F);

			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			float minU = textureatlassprite.getMinU();
			float minV = textureatlassprite.getMinV();
			float maxU = textureatlassprite.getMaxU();
			float maxV = textureatlassprite.getMaxV();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
			bufferbuilder.pos(-0.5D, -0.25D, 0.0D).tex((double)maxU, (double)maxV).normal(0.0F, 1.0F, 0.0F).endVertex();
			bufferbuilder.pos(0.5D, -0.25D, 0.0D).tex((double)minU, (double)maxV).normal(0.0F, 1.0F, 0.0F).endVertex();
			bufferbuilder.pos(0.5D, 0.75D, 0.0D).tex((double)minU, (double)minV).normal(0.0F, 1.0F, 0.0F).endVertex();
			bufferbuilder.pos(-0.5D, 0.75D, 0.0D).tex((double)maxU, (double)minV).normal(0.0F, 1.0F, 0.0F).endVertex();
			tessellator.draw();

			/*if(shaderGroup != null && ShaderHelper.shouldUseShaders()) {
				GlStateManager.matrixMode(5890);
				GlStateManager.pushMatrix();
				GlStateManager.loadIdentity();
				shaderGroup.render(partialTicks);
				GlStateManager.popMatrix();
			}*/

			GlStateManager.disableBlend();
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
			super.doRender(entity, x, y, z, entityYaw, partialTicks);
		}
	}

	@Override
	public void doRenderShadowAndFire(@Nonnull Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {}

	@Override
	@Nonnull
	protected ResourceLocation getEntityTexture(@Nonnull T entity) {
		return WILL_O_WISP_TEXTURE;
	}

}
