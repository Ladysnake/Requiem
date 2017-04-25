package ladysnake.tartaros.client.renders.blocks;

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
	    
	    GlStateManager.translate(x, y, z);
	    GlStateManager.rotate(180,1,0,0);
	    
	    FontRenderer fnt = mc.fontRendererObj;
	    float scale = 10 / fnt.FONT_HEIGHT;
	    GlStateManager.scale(scale, scale, scale);
	    GlStateManager.rotate(mc.getRenderManager().playerViewY + 180, 0.0F, 1.0F, 0.0F);
	    GlStateManager.rotate(-mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
	    
	    GlStateManager.translate(-fnt.getStringWidth(text) / 2, 0, 0);
	    fnt.drawString(text, 0, 0, 0x121212);
	    
	    GlStateManager.disableRescaleNormal();
	    GlStateManager.enableDepth();
	    Minecraft.getMinecraft().entityRenderer.enableLightmap();
	    
	    GlStateManager.popMatrix();
	}
}
