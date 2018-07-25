package ladysnake.dissolution.client.gui;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OverlaysRenderer {

    public static final OverlaysRenderer INSTANCE = new OverlaysRenderer();

    private static final ResourceLocation INCORPOREAL_PATH = new ResourceLocation(Reference.MOD_ID, "textures/gui/soul_overlay.png");

    private float b = 0.0F;

    void renderOverlays(RenderGameOverlayEvent.Post event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);
//        if (playerCorp.getCorporealityStatus().isIncorporeal() && playerCorp.getPossessed() == null && !player.isCreative()) {
//            drawIncorporealOverlay(event.getResolution());
//        }
        EntityLivingBase possessed = playerCorp.getPossessed();
        if (possessed != null && possessed.isBurning()) {
            this.renderFireInFirstPerson(event.getResolution());
        }
    }

    /**
     * Draws the blue overlay telling the player he's a ghost
     */
    private void drawIncorporealOverlay(ScaledResolution scaledRes) {
        final float inc = 0.001F;
        b += inc;

        GlStateManager.pushAttrib();
        GlStateManager.color((float) Math.cos(b), 1.0F, 1.0F, 0.5F);
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        Minecraft.getMinecraft().getTextureManager().bindTexture(INCORPOREAL_PATH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();

        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(0.0D, (double) scaledRes.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
        vertexbuffer.pos((double) scaledRes.getScaledWidth(), (double) scaledRes.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
        vertexbuffer.pos((double) scaledRes.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
        vertexbuffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.popAttrib();
    }

    private void renderFireInFirstPerson(ScaledResolution scaledRes)
    {
        GlStateManager.disableAlpha();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
        GlStateManager.depthFunc(519);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        for (int i = 0; i < 2; ++i)
        {
            GlStateManager.pushMatrix();
            TextureAtlasSprite textureatlassprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            float f1 = textureatlassprite.getMinU();
            float f2 = textureatlassprite.getMaxU();
            float f3 = textureatlassprite.getMinV();
            float f4 = textureatlassprite.getMaxV();
            GlStateManager.translate((float)(-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            GlStateManager.rotate((float)(i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(0.0D, 1.75*scaledRes.getScaledHeight(), -90.0D).tex((double)f2, (double)f4).endVertex();
            bufferbuilder.pos((double) scaledRes.getScaledWidth(), 1.75*scaledRes.getScaledHeight(), -90.0D).tex((double)f1, (double)f4).endVertex();
            bufferbuilder.pos((double) scaledRes.getScaledWidth(), 0.25*scaledRes.getScaledHeight(), -90.0D).tex((double)f1, (double)f3).endVertex();
            bufferbuilder.pos(0.0D, 0.25*scaledRes.getScaledHeight(), -90.0D).tex((double)f2, (double)f3).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
        GlStateManager.enableAlpha();
    }


}
