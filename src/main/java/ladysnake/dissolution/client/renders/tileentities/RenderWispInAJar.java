package ladysnake.dissolution.client.renders.tileentities;

import ladysnake.dissolution.client.renders.entities.RenderWillOWisp;
import ladysnake.dissolution.common.entity.souls.AbstractSoul;
import ladysnake.dissolution.common.entity.souls.EntityFleetingSoul;
import ladysnake.dissolution.common.tileentities.TileEntityWispInAJar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderWispInAJar extends TileEntitySpecialRenderer<TileEntityWispInAJar> {

    @Override
    public void render(TileEntityWispInAJar te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        int time = (int) Minecraft.getMinecraft().player.world.getWorldTime() % 34;
        double translation = Math.abs((time-17.0) / 34.0);

        GlStateManager.translate((float) x+0.4+Math.cos(translation)*0.1, (float) y + 0.1f+0.1*translation, (float) z+0.4+0.2*Math.sin(translation));

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        GlStateManager.rotate(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (renderManager.options.thirdPersonView == 2 ? -1 : 1) * -renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        this.bindTexture(RenderWillOWisp.WILL_O_WISP_TEXTURE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        int i = time % 34 / 2;
        float minU = (i / 5 * 16) / 80f;
        float minV = (i % 4 * 16) / 80f;
        float maxU = (i / 5 * 16 + 16) / 80f;
        float maxV = (i % 4 * 16 + 16) / 80f;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        bufferbuilder.pos(-0.5D, -0.25D, 0.0D).tex((double) maxU, (double) maxV).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(0.5D, -0.25D, 0.0D).tex((double) minU, (double) maxV).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(0.5D, 0.75D, 0.0D).tex((double) minU, (double) minV).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.pos(-0.5D, 0.75D, 0.0D).tex((double) maxU, (double) minV).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.draw();

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
