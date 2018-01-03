package ladysnake.dissolution.client.gui;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.init.ModFluids;
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
    private static final ResourceLocation RES_MERCURY_OVERLAY = new ResourceLocation(Reference.MOD_ID, "textures/gui/soul_overlay.png");

    private float b = 0.0F;

    void renderOverlays(RenderGameOverlayEvent.Post event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);
        if (playerCorp.getCorporealityStatus().isIncorporeal() && playerCorp.getPossessed() == null)
            drawIncorporealOverlay(event.getResolution());
        if (player.world.getBlockState(player.getPosition().up()).getBlock() == ModFluids.MERCURY.fluidBlock())
            renderWaterOverlayTexture(event.getPartialTicks());
        if (playerCorp.getPossessed() instanceof EntityLivingBase && ((EntityLivingBase)playerCorp.getPossessed()).isBurning())
            this.renderFireInFirstPerson(event.getResolution());
    }

    private void renderWaterOverlayTexture(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(RES_MERCURY_OVERLAY);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        float f = mc.player.getBrightness();
        GlStateManager.color(f, f, f, 0.5F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        float texMin = 0f;
        float texMax = 4.0F;
        float xMin = -1.0F;
        float xMax = 1.0F;
        float yMin = -1.0F;
        float yMax = 1.0F;
        float z = -0.5F;
        float f7 = -mc.player.rotationYaw / 64.0F;
        float f8 = mc.player.rotationPitch / 64.0F;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xMin, yMin, z).tex((texMax + f7), (texMax + f8)).endVertex();
        bufferbuilder.pos(xMax, yMin, z).tex((texMin + f7), (texMax + f8)).endVertex();
        bufferbuilder.pos(xMax, yMax, z).tex((texMin + f7), (texMin + f8)).endVertex();
        bufferbuilder.pos(xMin, yMax, z).tex((texMax + f7), (texMin + f8)).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
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
