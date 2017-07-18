package ladysnake.dissolution.client.gui;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.init.ModFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class OverlaysRenderer {
	
	public static final OverlaysRenderer INSTANCE = new OverlaysRenderer();
	
	private static final ResourceLocation INCORPOREAL_PATH = new ResourceLocation(Reference.MOD_ID, "textures/gui/soul_overlay.png");
	private static final ResourceLocation RES_MERCURY_OVERLAY = new ResourceLocation(Reference.MOD_ID, "textures/gui/soul_overlay.png");
	
	private float inc = 0.001F;
	private float b = 0.0F;
	private boolean shade = false;
	
	public void renderOverlays(RenderGameOverlayEvent.Post event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);
		if(playerCorp.isIncorporeal())
			drawIncorporealOverlay(event.getResolution(), playerCorp.isIntangible());
		if(player.world.getBlockState(player.getPosition().up()).getBlock() == ModFluids.MERCURY.fluidBlock()) {
			renderWaterOverlayTexture(event.getPartialTicks());
//			System.out.println("in mercury");
		}
	}
	
	private void renderWaterOverlayTexture(float partialTicks)
    {
		Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(RES_MERCURY_OVERLAY);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        float f = mc.player.getBrightness();
        GlStateManager.color(f, f, f, 0.5F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        float f1 = 4.0F;
        float f2 = -1.0F;
        float f3 = 1.0F;
        float f4 = -1.0F;
        float f5 = 1.0F;
        float f6 = -0.5F;
        float f7 = -mc.player.rotationYaw / 64.0F;
        float f8 = mc.player.rotationPitch / 64.0F;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-1.0D, -1.0D, -0.5D).tex((double)(4.0F + f7), (double)(4.0F + f8)).endVertex();
        bufferbuilder.pos(1.0D, -1.0D, -0.5D).tex((double)(0.0F + f7), (double)(4.0F + f8)).endVertex();
        bufferbuilder.pos(1.0D, 1.0D, -0.5D).tex((double)(0.0F + f7), (double)(0.0F + f8)).endVertex();
        bufferbuilder.pos(-1.0D, 1.0D, -0.5D).tex((double)(4.0F + f7), (double)(0.0F + f8)).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
    }
	
	/**
	 * Draws the blue overlay telling the player he's a ghost
	 * @param scaledRes
	 */
	public void drawIncorporealOverlay(ScaledResolution scaledRes, boolean intangible)
    {
		
		b += inc;
		//System.out.println(Math.cos(b));
		
		GlStateManager.pushAttrib();
		GlStateManager.color((float) Math.cos(b), 1.0F, 1.0F, intangible ? 0.8F : 0.5F);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
				
        Minecraft.getMinecraft().getTextureManager().bindTexture(INCORPOREAL_PATH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(0.0D, (double)scaledRes.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
        vertexbuffer.pos((double)scaledRes.getScaledWidth(), (double)scaledRes.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
        vertexbuffer.pos((double)scaledRes.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
        vertexbuffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
		
        GlStateManager.popAttrib();
		/*
		GlStateManager.pushAttrib();
		GlStateManager.depthMask(false);
        GlStateManager.depthFunc(514);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        this.mc.getTextureManager().bindTexture(ENCHANTED_ITEM_GLINT_RES);
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        GlStateManager.popAttrib();*/
    }

}
