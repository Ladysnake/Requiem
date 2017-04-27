package ladysnake.tartaros.client.renders.blocks;

import java.util.Vector;

import org.lwjgl.opengl.GL11;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class RenderSoulAnchor extends TileEntitySpecialRenderer {
	
	private static final ResourceLocation texture = new ResourceLocation(Reference.MOD_ID + ":textures/blocks/soul_anchor_special_render.png");
	private static final String text = "ancre";
	private static int height = 2, width = 2;
	
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		
		//System.out.println(text);
		Minecraft mc = Minecraft.getMinecraft();
		
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(mc.player);
    	if(!playerCorp.isIncorporeal()) return;
		
		GlStateManager.pushMatrix();
		
		Minecraft.getMinecraft().entityRenderer.disableLightmap();
		
		GlStateManager.translate(x, y, z);
		GlStateManager.enableRescaleNormal();
	    GlStateManager.disableDepth();
	    GlStateManager.disableLighting();
	    
	    GlStateManager.disableTexture2D();

	    Minecraft.getMinecraft().renderEngine.bindTexture(texture);
	    //GlStateManager.bindTexture(texture);		//TODO
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
	    GlStateManager.color(0.8f, 0.8f, 1f, 1.0f);
	    Tessellator tessellator = Tessellator.getInstance();
	    VertexBuffer tes = tessellator.getBuffer();
	    
	    tes.setTranslation(0.0, -Minecraft.getMinecraft().player.eyeHeight, 0.0);
	    
	    //First face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x, y + height, z).endVertex();
	    tes.pos(x + width, y + height, z).endVertex();
	    tes.pos(x + width, y, z).endVertex();
	    tes.pos(x, y, z).endVertex();
	    
	    tessellator.draw();
	    
	    //Second face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x, y, z + width).endVertex();
	    tes.pos(x, y + height, z + width).endVertex();
	    tes.pos(x, y + height, z).endVertex();
	    tes.pos(x, y, z).endVertex();
	    
	    tessellator.draw();
	    
	    //Third face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x + width, y + height, z + width).endVertex();
	    tes.pos(x, y + height, z + width).endVertex();
	    tes.pos(x, y, z + width).endVertex();
	    tes.pos(x + width, y, z + width).endVertex();
	    
	    tessellator.draw();
	    
	    //Fourth face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x + width, y, z).endVertex();
	    tes.pos(x + width, y + height, z).endVertex();
	    tes.pos(x + width, y + height, z + width).endVertex();
	    tes.pos(x + width, y, z + width).endVertex();
	    
	    tessellator.draw();
	    
	    //Bottom face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x, y, z).endVertex();
	    tes.pos(x + width, y, z).endVertex();
	    tes.pos(x + width, y, z + width).endVertex();
	    tes.pos(x, y, z + width).endVertex();
	    
	    tessellator.draw();
	    
	    //Top face
	    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
	    tes.pos(x, y + height, z).endVertex();
	    tes.pos(x, y + height, z + width).endVertex();
	    tes.pos(x + width, y + height, z + width).endVertex();
	    tes.pos(x + width, y + height, z).endVertex();
	    
	    tessellator.draw();

	    tes.setTranslation(0, 0, 0);
	    GlStateManager.enableTexture2D();

	    
	    /*GlStateManager.translate(x, y, z);
	    GlStateManager.rotate(180,1,0,0);
	    
	    FontRenderer fnt = mc.fontRendererObj;
	    float scale = 10 / fnt.FONT_HEIGHT;
	    GlStateManager.scale(scale, scale, scale);
	    GlStateManager.rotate(mc.getRenderManager().playerViewY + 180, 0.0F, 1.0F, 0.0F);
	    GlStateManager.rotate(-mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
	    
	    GlStateManager.translate(-fnt.getStringWidth(text) / 2, 0, 0);
	    fnt.drawString(text, 0, 0, 0x121212);
	    */
	    
	    GlStateManager.disableRescaleNormal();
	    GlStateManager.enableDepth();
	    Minecraft.getMinecraft().entityRenderer.enableLightmap();
	    
	    GlStateManager.popMatrix();
	}
}
