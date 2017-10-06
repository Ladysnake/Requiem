 package ladysnake.dissolution.client.gui;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

import static net.minecraftforge.client.GuiIngameForge.left_height;

 @SideOnly(Side.CLIENT)
public class GuiIncorporealOverlay extends Gui {
	
	private static final ResourceLocation ORIGIN_PATH = new ResourceLocation(Reference.MOD_ID, "textures/gui/soul_compass.png");
	private static final ResourceLocation MAGIC_BAR_PATH = new ResourceLocation(Reference.MOD_ID, "textures/gui/soul_magic_bar.png");
	private static final ResourceLocation ECTOPLASM_ICONS = new ResourceLocation(Reference.MOD_ID, "textures/gui/ectoplasm_icons.png");
	private Random rand = new Random();

	private Minecraft mc;
	
	public GuiIncorporealOverlay(Minecraft mc) {
		super();
		this.mc = mc;
	}
	
	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent.Post event) {
		final IIncorporealHandler pl = CapabilityIncorporealHandler.getHandler(this.mc.player);
		if (event.getType() == ElementType.EXPERIENCE) {
			OverlaysRenderer.INSTANCE.renderOverlays(event);

			/* Draw Incorporeal Ingame Gui */
			if (pl.getCorporealityStatus().isIncorporeal()) {
				if (DissolutionConfig.client.soulCompass)
					this.drawOriginIndicator(event.getResolution());
			}

			if (pl.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.ECTOPLASM) {
				this.drawEctoplasmHealthBar(this.mc.player, event.getResolution());
			}
		}
	}

	@SubscribeEvent
	public void onRenderHealth(RenderGameOverlayEvent.Pre event) {
		final IIncorporealHandler pl = CapabilityIncorporealHandler.getHandler(this.mc.player);
		if(event.getType() == ElementType.HEALTH)
			event.setCanceled(pl.getCorporealityStatus().isIncorporeal());
	}
	
	/**
	 * Draws the HUD indicating 0,0
	 */
	private void drawOriginIndicator(ScaledResolution scaledRes) {
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
		
        this.mc.getTextureManager().bindTexture(ORIGIN_PATH);
		this.drawTexturedModalRect(i, j, 0, 0, compassWidth, 20);
		
		if(isInFieldOfView) {
			this.drawTexturedModalRect(i + 3 + (int)Math.round((angleToOrigin - angleLeftVision) / (angleRightVision - angleLeftVision) * (compassWidth - 13)), j + 5, 200, 0, 7, 10);
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

	private void drawEctoplasmHealthBar(EntityPlayer player, ScaledResolution scaledResolution) {
		this.mc.getTextureManager().bindTexture(ECTOPLASM_ICONS);
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();

		GlStateManager.pushAttrib();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.enableBlend();

		int health = (int) Math.ceil(player.getHealth());
		IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		float healthMax = (float)attrMaxHealth.getAttributeValue();
		int healthRows = MathHelper.ceil((healthMax) / 2.0F / 10.0F);
		int rowHeight = Math.max(10 - (healthRows - 2), 3);
		int left = width / 2 - 91;
		int top = height - left_height;
		left_height += (healthRows * rowHeight);
		if (rowHeight != 10) left_height += 10 - rowHeight;

		final int TOP = 0;
		final int MARGIN = 0;

		for (int i = MathHelper.ceil((healthMax) / 2.0F) - 1; i >= 0; --i) {
			int row = MathHelper.ceil((float) (i + 1) / 10.0F) - 1;
			int x = left + i % 10 * 8;
			int y = top - row * rowHeight;

			if (health <= 4) y += rand.nextInt(2);

			if (i * 2 + 1 < health)
				drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9); //4
			else if (i * 2 + 1 == health)
				drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9); //5

		}
		GlStateManager.popAttrib();
	}
	
	public void drawMagicBar(ScaledResolution scaledRes) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(MAGIC_BAR_PATH);
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
