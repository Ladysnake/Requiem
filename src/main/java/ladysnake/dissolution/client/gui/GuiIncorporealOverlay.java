package ladysnake.dissolution.client.gui;

import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.tileentities.TileEntitySoulAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiIncorporealOverlay extends Gui {
	
	private static final ResourceLocation ORIGIN_PATH = new ResourceLocation(Reference.MOD_ID, "textures/gui/soul_compass_legacy.png");
	private static final ResourceLocation MAGIC_BAR_PATH = new ResourceLocation(Reference.MOD_ID, "textures/gui/soul_magic_bar.png");
	protected static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation(Reference.MOD_ID, "textures/misc/enchanted_item_glint.png");
	private boolean usingShader;

	private Minecraft mc;
	
	public GuiIncorporealOverlay(Minecraft mc) {
		super();
		this.mc = mc;
		this.usingShader = false;
	}
	
	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent.Post event) {
		if (event.getType() != ElementType.EXPERIENCE) return;
		final IIncorporealHandler pl = CapabilityIncorporealHandler.getHandler(this.mc.player);
		
		OverlaysRenderer.INSTANCE.renderOverlays(event);
		
		/* Draw Incorporeal Ingame Gui */
		if(pl.isIncorporeal()) {
	        if(DissolutionConfig.soulCompass)
				this.drawOriginIndicator(event.getResolution());
	        //this.drawMagicBar(event.getResolution());
	        
		}
        if(pl.isSoulCandleNearby(1)) {
        	if(!usingShader) {
        		Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/desaturate.json"));
        		usingShader = true;
        	}
        }
        else if(pl.isSoulCandleNearby(2)){
        	if(!usingShader) {
        		Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/deconverge.json"));
    			usingShader = true;
        	}
        }
        else {
        	if(usingShader) {
        		Minecraft.getMinecraft().entityRenderer.stopUseShader();
        		usingShader = false;
        	}
        }
	}
	
	/**
	 * Draws the HUD indicating 0,0
	 * @param scaledRes
	 */
	public void drawOriginIndicator(ScaledResolution scaledRes) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		double fov = this.mc.gameSettings.fovSetting;
		double angleToOrigin;
		angleToOrigin = (180 - (Math.atan2(player.posX, player.posZ)) * (180 / Math.PI)) % 360D;
		double anglePlayer;
		anglePlayer = player.rotationYaw % 360;
		anglePlayer = (anglePlayer < 0) ? anglePlayer + 360 : anglePlayer;
		double angleLeftVision = (anglePlayer - (fov / 2.0D)) % 360D;
		double angleRightVision = (anglePlayer + (fov / 2.0D)) % 360D;
		boolean isInFieldOfView = angleToOrigin > angleLeftVision && angleToOrigin < angleRightVision;
		
		int i = scaledRes.getScaledWidth() / 2 - 100;
		int j = 10;
		int compassWidth = 200;

		GlStateManager.pushAttrib();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(ORIGIN_PATH);
		this.drawTexturedModalRect(i, j, 0, 0, compassWidth, 20);
		//this.mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/furnace.png"));
		//this.drawTexturedModalRect(0,0,0,0,200,200);
		
		if(isInFieldOfView) {
			this.drawTexturedModalRect(i + 3 + (int)Math.round((angleToOrigin - angleLeftVision) / (angleRightVision - angleLeftVision) * (compassWidth - 13)), j + 5, 200, 0, 7, 10);
		}
		
		if(!DissolutionConfig.soulCompassAnchors) return;
		
		for(TileEntity te : mc.player.world.loadedTileEntityList) {
			if(te instanceof TileEntitySoulAnchor) {
				if(Math.sqrt(Math.pow(te.getPos().getX() - mc.player.posX, 2) + Math.pow(te.getPos().getZ() - mc.player.posZ, 2)) < 100) {
					double angleToTE = (180 - (Math.atan2(player.posX - te.getPos().getX(), player.posZ - te.getPos().getZ())) * (180 / Math.PI)) % 360D;
					if (angleToTE > angleLeftVision && angleToTE < angleRightVision) {
						this.drawTexturedModalRect(i + 3 + (int)Math.round((angleToTE - angleLeftVision) / (angleRightVision - angleLeftVision) * (compassWidth - 13)), j + 5, 207, 0, 7, 10);
					}
				}
			}
		}

		for(Entity te : mc.player.world.loadedEntityList) {
			if(te instanceof EntityPlayerCorpse) {
				if(mc.player.getUniqueID().equals(((EntityPlayerCorpse) te).getPlayer())) {
					double angleToTE = (180 - (Math.atan2(player.posX - te.posX, player.posZ - te.posZ)) * (180 / Math.PI)) % 360D;
					if (angleToTE > angleLeftVision && angleToTE < angleRightVision) {
						this.drawTexturedModalRect(i + 3 + (int)Math.round((angleToTE - angleLeftVision) / (angleRightVision - angleLeftVision) * (compassWidth - 13)), j + 5, 214, 0, 7, 10);
					}
				}
			}
		}
		
        GlStateManager.popAttrib();
	}
	
	public void drawMagicBar(ScaledResolution scaledRes) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(MAGIC_BAR_PATH);
        EntityPlayer entityplayer = (EntityPlayer)this.mc.getRenderViewEntity();
        int i = scaledRes.getScaledWidth() / 2;
        float f = this.zLevel;
        int j = 182;
        int k = 91;
        this.zLevel = -90.0F;
        this.drawTexturedModalRect(i - 91, scaledRes.getScaledHeight() - 22, 0, 0, 182, 22);
        int currentItem = 4;
        this.drawTexturedModalRect(i - 91 - 1 + currentItem * 20, scaledRes.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
        
	}
}
