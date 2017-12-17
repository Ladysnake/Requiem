package ladysnake.dissolution.client.gui;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.entity.minion.*;
import ladysnake.dissolution.common.registries.EctoplasmCorporealityStatus;
import ladysnake.dissolution.common.tileentities.TileEntityLamentStone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class GuiIncorporealOverlay extends Gui {

    private static final ResourceLocation WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation ORIGIN_PATH = new ResourceLocation(Reference.MOD_ID, "textures/gui/soul_compass.png");
    private static final ResourceLocation ECTOPLASM_ICONS = new ResourceLocation(Reference.MOD_ID, "textures/gui/icons.png");
    private final Random rand = new Random();

    private final Minecraft mc;

    public GuiIncorporealOverlay(Minecraft mc) {
        super();
        this.mc = mc;
    }

    @SubscribeEvent
    public void onRenderExperienceBar(RenderGameOverlayEvent.Post event) {
        final IIncorporealHandler pl = CapabilityIncorporealHandler.getHandler(this.mc.player);
        if (event.getType() == ElementType.ALL) {
            OverlaysRenderer.INSTANCE.renderOverlays(event);

			/* Draw Incorporeal Ingame Gui */
            if (pl.getCorporealityStatus().isIncorporeal() && pl.getPossessed() == null) {
                if (Dissolution.config.client.soulCompass)
                    this.drawOriginIndicator(event.getResolution());
            }

            if (this.mc.playerController.shouldDrawHUD() && this.mc.getRenderViewEntity() instanceof EntityPlayer && pl.getCorporealityStatus() == EctoplasmCorporealityStatus.ECTOPLASM) {
                this.drawCustomHealthBar(this.mc.player, event.getResolution(), 0);
            } else if (this.mc.playerController.shouldDrawHUD()) {
                IPossessable possessed = pl.getPossessed();
                if (possessed instanceof EntityLivingBase && ((EntityLivingBase) possessed).getHealth() > 0) {
                    int textureRow = 0;
                    if (possessed instanceof EntityMinionPigZombie) textureRow = 1;
                    else if (possessed instanceof EntityMinionZombie && ((EntityMinionZombie) possessed).isHusk())
                        textureRow = 2;
                    else if (possessed instanceof EntityMinionWitherSkeleton) textureRow = 4;
                    else if (possessed instanceof EntityMinionStray) textureRow = 5;
                    else if (possessed instanceof EntityMinionSkeleton) textureRow = 3;

                    this.drawCustomHealthBar((EntityLivingBase) pl.getPossessed(), event.getResolution(), textureRow);
                    this.renderHotbar(event.getResolution(), event.getPartialTicks());
                }
            } else if (Minecraft.getMinecraft().player.isCreative() && pl.getPossessed() != null)
                this.renderHotbar(event.getResolution(), event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public void onRenderHealth(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == ElementType.HEALTH) {
            final IIncorporealHandler pl = CapabilityIncorporealHandler.getHandler(this.mc.player);
//            event.setCanceled(pl.getCorporealityStatus().isIncorporeal());
        }
    }

    /**
     * Draws the HUD indicating 0,0
     */
    private void drawOriginIndicator(ScaledResolution scaledRes) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        double fov = this.mc.gameSettings.fovSetting;
//		double angleToOrigin;
//		angleToOrigin = (180 - (Math.atan2(player.posX, player.posZ)) * (180 / Math.PI)) % 360D;
        double anglePlayer;
        anglePlayer = player.rotationYaw % 360;
        anglePlayer = (anglePlayer < 0) ? anglePlayer + 360 : anglePlayer;
        double angleLeftVision = (anglePlayer - (fov / 2.0D)) % 360D;
        double angleRightVision = (anglePlayer + (fov / 2.0D)) % 360D;
//		boolean isInFieldOfView = angleToOrigin > angleLeftVision && angleToOrigin < angleRightVision;

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

		/*if(isInFieldOfView) {
			this.drawTexturedModalRect(i + 3 + (int)Math.round((angleToOrigin - angleLeftVision) / (angleRightVision - angleLeftVision) * (compassWidth - 13)), j + 5, 200, 0, 7, 10);
		}*/

        for (TileEntity te : mc.player.world.loadedTileEntityList) {
            renderLamentStones:
            if (te instanceof TileEntityLamentStone && Dissolution.config.client.lamentStonesCompassDistance > 0) {
                double lengthX = player.posX - te.getPos().getX();
                double lengthY = player.posZ - te.getPos().getZ();
                if (lengthX * lengthX + lengthY * lengthY > Dissolution.config.client.lamentStonesCompassDistance * Dissolution.config.client.lamentStonesCompassDistance)
                    break renderLamentStones;
                double angleToTE = (180 - (Math.atan2(lengthX, lengthY)) * (180 / Math.PI)) % 360D;
                if (angleToTE > angleLeftVision && angleToTE < angleRightVision) {
                    this.drawTexturedModalRect(i + 3 + (int) Math.round((angleToTE - angleLeftVision) / (angleRightVision - angleLeftVision) * (compassWidth - 13)), j + 5, 200, 0, 7, 10);
                }
            }
        }

        for (Entity te : mc.player.world.loadedEntityList) {
            if (te instanceof EntityPlayerCorpse) {
                if (mc.player.getUniqueID().equals(((EntityPlayerCorpse) te).getPlayer())) {
                    double angleToTE = (180 - (Math.atan2(player.posX - te.posX, player.posZ - te.posZ)) * (180 / Math.PI)) % 360D;
                    if (angleToTE > angleLeftVision && angleToTE < angleRightVision) {
                        this.drawTexturedModalRect(i + 3 + (int) Math.round((angleToTE - angleLeftVision) / (angleRightVision - angleLeftVision) * (compassWidth - 13)), j + 5, 207, 0, 7, 10);
                    }
                }
            }
        }

        GlStateManager.popAttrib();
    }

    private void drawCustomHealthBar(EntityLivingBase player, ScaledResolution scaledResolution, int textureRow) {
        this.mc.getTextureManager().bindTexture(ECTOPLASM_ICONS);
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        GlStateManager.pushAttrib();
//		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
//		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
//				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
//				GlStateManager.DestFactor.ZERO);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        int health = (int) Math.ceil(player.getHealth());
        IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        float healthMax = (float) attrMaxHealth.getAttributeValue();
        int healthRows = MathHelper.ceil((healthMax) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        int left = width / 2 - 91;
        int top = height - GuiIngameForge.left_height;
        GuiIngameForge.left_height += (healthRows * rowHeight);
        if (rowHeight != 10) GuiIngameForge.left_height += 10 - rowHeight;

        final int TOP = textureRow * 9;
        final int MARGIN = 0;

        for (int i = MathHelper.ceil((healthMax) / 2.0F) - 1; i >= 0; --i) {
            int row = MathHelper.ceil((float) (i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) y += rand.nextInt(2);

            if (i * 2 + 1 < health)
                drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9);
            else if (i * 2 + 1 == health)
                drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9);

        }
        GlStateManager.popAttrib();
    }

    private void renderHotbar(ScaledResolution sr, float partialTicks) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(WIDGETS_TEX_PATH);
        EntityPlayer entityplayer = this.mc.player;
        ItemStack itemstack = entityplayer.getHeldItemOffhand();
        EnumHandSide enumhandside = entityplayer.getPrimaryHand().opposite();
        int i = sr.getScaledWidth() / 2;
        float f = this.zLevel;
        int j = 182;
        int k = 91;
        this.zLevel = -90.0F;
        this.drawTexturedModalRect(i - k, sr.getScaledHeight() - 22, 0, 0, j, 22);
        this.drawTexturedModalRect(i - k - 1 + entityplayer.inventory.currentItem * 20, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);

        if (!itemstack.isEmpty()) {
            if (enumhandside == EnumHandSide.LEFT) {
                this.drawTexturedModalRect(i - 91 - 29, sr.getScaledHeight() - 23, 24, 22, 29, 24);
            } else {
                this.drawTexturedModalRect(i + 91, sr.getScaledHeight() - 23, 53, 22, 29, 24);
            }
        }

        this.zLevel = f;
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.enableGUIStandardItemLighting();

        for (int l = 0; l < 9; ++l) {
            int i1 = i - 90 + l * 20 + 2;
            int j1 = sr.getScaledHeight() - 16 - 3;
            this.renderHotbarItem(i1, j1, partialTicks, entityplayer, entityplayer.inventory.mainInventory.get(l));
        }

        if (!itemstack.isEmpty()) {
            int l1 = sr.getScaledHeight() - 16 - 3;

            if (enumhandside == EnumHandSide.LEFT) {
                this.renderHotbarItem(i - 91 - 26, l1, partialTicks, entityplayer, itemstack);
            } else {
                this.renderHotbarItem(i + 91 + 10, l1, partialTicks, entityplayer, itemstack);
            }
        }

        if (this.mc.gameSettings.attackIndicator == 2) {
            float f1 = this.mc.player.getCooledAttackStrength(0.0F);

            if (f1 < 1.0F) {
                int i2 = sr.getScaledHeight() - 20;
                int j2 = i + 91 + 6;

                if (enumhandside == EnumHandSide.RIGHT) {
                    j2 = i - 91 - 22;
                }

                this.mc.getTextureManager().bindTexture(Gui.ICONS);
                int k1 = (int) (f1 * 19.0F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.drawTexturedModalRect(j2, i2, 0, 94, 18, 18);
                this.drawTexturedModalRect(j2, i2 + 18 - k1, 18, 112 - k1, 18, k1);
            }
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }

    private void renderHotbarItem(int p_184044_1_, int p_184044_2_, float p_184044_3_, EntityPlayer player, ItemStack stack) {
        if (!stack.isEmpty()) {
            float f = (float) stack.getAnimationsToGo() - p_184044_3_;

            if (f > 0.0F) {
                GlStateManager.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                GlStateManager.translate((float) (p_184044_1_ + 8), (float) (p_184044_2_ + 12), 0.0F);
                GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                GlStateManager.translate((float) (-(p_184044_1_ + 8)), (float) (-(p_184044_2_ + 12)), 0.0F);
            }

            this.mc.getRenderItem().renderItemAndEffectIntoGUI(player, stack, p_184044_1_, p_184044_2_);

            if (f > 0.0F) {
                GlStateManager.popMatrix();
            }

            this.mc.getRenderItem().renderItemOverlays(this.mc.fontRenderer, stack, p_184044_1_, p_184044_2_);
        }
    }

}
