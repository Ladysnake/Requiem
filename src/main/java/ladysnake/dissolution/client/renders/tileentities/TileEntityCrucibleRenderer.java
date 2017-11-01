package ladysnake.dissolution.client.renders.tileentities;

import ladysnake.dissolution.common.tileentities.TileEntityCrucible;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.lwjgl.opengl.GL11;

/**
 * This class has been adapted from embers' source code under GNU Lesser General Public License 2.1
 * https://github.com/RootsTeam/Embers/blob/1.12/src/main/java/teamroots/embers/tileentity/TileEntityStampBaseRenderer.java
 * @author Elucent
 *
 */
public class TileEntityCrucibleRenderer extends TileEntitySpecialRenderer<TileEntityCrucible> {

    @Override
    public void render(TileEntityCrucible te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        FluidTank fluidHandler = te.getFluidInventory();
        FluidStack fluidStack = fluidHandler.getFluid();
        if(fluidStack != null && fluidStack.getFluid() != null) {
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStack.getFluid().getStill().toString());
            int color = fluidStack.getFluid().getColor();
            double amount = fluidHandler.getFluidAmount();
            double capacity = fluidHandler.getCapacity();
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (color >> 24) & 0xFF;
            int i = te.getWorld().getCombinedLight(te.getPos(), fluidStack.getFluid().getLuminosity());
            double minU = sprite.getMinU();
            double minV = sprite.getMinV();
            double maxU = sprite.getMaxU();
            double maxV = sprite.getMaxV();
            int lightX = i >> 0x10 & 0xFFFF;
            int lightY = i & 0xFFFF;
            GlStateManager.disableCull();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            GL11.glTranslated(0.5, 0.5, 0.5);
            GL11.glRotated(180, 1, 0, 0);

            GL11.glTranslated(-0.5, -0.5, -0.5);

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
            buffer.pos(0.25, 0.75+0.1875*((float)amount/(float)capacity), 0.25).tex(minU, minV).lightmap(lightX,lightY).color(r,g,b,a).endVertex();
            buffer.pos(0.75, 0.75+0.1875*((float)amount/(float)capacity), 0.25).tex(maxU, minV).lightmap(lightX,lightY).color(r,g,b,a).endVertex();
            buffer.pos(0.75, 0.75+0.1875*((float)amount/(float)capacity), 0.75).tex(maxU, maxV).lightmap(lightX,lightY).color(r,g,b,a).endVertex();
            buffer.pos(0.25, 0.75+0.1875*((float)amount/(float)capacity), 0.75).tex(minU, maxV).lightmap(lightX,lightY).color(r,g,b,a).endVertex();
            tess.draw();
            GL11.glPopMatrix();

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
        }
    }
}
