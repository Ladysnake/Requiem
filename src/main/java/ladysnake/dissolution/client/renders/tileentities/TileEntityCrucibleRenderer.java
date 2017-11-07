package ladysnake.dissolution.client.renders.tileentities;

import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import ladysnake.dissolution.common.tileentities.TileEntityCrucible;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
        IFluidHandler fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        GenericStackInventory<EnumPowderOres> powderInv = CapabilityGenericInventoryProvider.getInventory(te, EnumPowderOres.class);
        if(fluidHandler instanceof FluidTank || powderInv != null) {
            GlStateManager.disableCull();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);

            if(fluidHandler instanceof FluidTank) {
                FluidTank fluidTank = (FluidTank) fluidHandler;
                FluidStack fluidStack = fluidTank.getFluid();
                if(fluidStack != null) {
                    TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStack.getFluid().getStill().toString());
                    int color = fluidStack.getFluid().getColor();
                    double amount = fluidTank.getFluidAmount();
                    double capacity = fluidTank.getCapacity();
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;
                    int a = (color >> 24) & 0xFF;
                    int i = te.getWorld().getCombinedLight(te.getPos(), fluidStack.getFluid().getLuminosity());
                    renderLevel(amount, capacity, sprite, i, r, g, b, a);
                }
            }
            if(powderInv != null) {
                double amount = powderInv.getTotalAmount();
                double capacity = powderInv.getSlotLimit(0);
                GlStateManager.disableBlend();
                TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/sand");
                int i = te.getWorld().getLight(te.getPos());
                if(amount > 0)
                    renderLevel(amount, capacity, sprite, i, 0xFF, 0xFF, 0xFF, 0xFF);
            }

            GlStateManager.popMatrix();

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
        }
        if(!te.getContent().isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate(0.5, 0.15, 0.4);
            GlStateManager.scale(0.8,0.8,0.8);
            GlStateManager.rotate(90, 1, 0, 0);
            Minecraft.getMinecraft().getRenderItem().renderItem(te.getContent(), ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
    }

    protected static void renderLevel(double amount, double capacity, TextureAtlasSprite sprite, int combinedLight, int r, int g, int b, int a) {
        int lightX = combinedLight >> 0x10 & 0xFFFF;
        int lightY = combinedLight & 0xFFFF;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        buffer.pos(0.25, 0.1 + 0.5*((float)amount/(float)capacity), 0.25).tex(sprite.getMinU(), sprite.getMinV()).lightmap(lightX,lightY).color(r,g,b,a).endVertex();
        buffer.pos(0.75, 0.1 + 0.5*((float)amount/(float)capacity), 0.25).tex(sprite.getMaxU(), sprite.getMinV()).lightmap(lightX,lightY).color(r,g,b,a).endVertex();
        buffer.pos(0.75, 0.1 + 0.5*((float)amount/(float)capacity), 0.75).tex(sprite.getMaxU(), sprite.getMaxV()).lightmap(lightX,lightY).color(r,g,b,a).endVertex();
        buffer.pos(0.25, 0.1 + 0.5*((float)amount/(float)capacity), 0.75).tex(sprite.getMinU(), sprite.getMaxV()).lightmap(lightX,lightY).color(r,g,b,a).endVertex();
        tess.draw();
    }
}
