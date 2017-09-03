package ladysnake.dissolution.client.renders.blocks;

import java.util.List;

import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3i;

public class RenderModularMachine extends TileEntitySpecialRenderer<TileEntityModularMachine> {

	@Override
	public void render(TileEntityModularMachine te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        
        // Translate to the location of our tile entity
        GlStateManager.translate(x, y, z);
        GlStateManager.disableRescaleNormal();

        renderParts(te);

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
	}
	
	@Override
	public void renderTileEntityFast(TileEntityModularMachine te, double x, double y, double z, float partialTicks,
			int destroyStage, float partial, BufferBuilder buffer) {
		//TODO render stuff
		List<BakedQuad> quads = DissolutionModelLoader.getModel(DissolutionModelLoader.CONTAINER).getQuads(null, null, te.getWorld().rand.nextLong());
		for(BakedQuad quad : quads)
			renderQuad(buffer, quad, -1);
	}
	
	private void putQuadNormal(BufferBuilder renderer, BakedQuad quad)
    {
        Vec3i vec3i = quad.getFace().getDirectionVec();
        renderer.putNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
    }

    private void renderQuad(BufferBuilder renderer, BakedQuad quad, int color)
    {
        renderer.addVertexData(quad.getVertexData());
        renderer.putColor4(color);
        this.putQuadNormal(renderer, quad);
    }
	
	private void renderParts(TileEntityModularMachine te) {
		ItemStack stack = new ItemStack(ModItems.MODULAR_CONTAINER);
		RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();
        GlStateManager.pushMatrix();
        // Translate to the center of the block and .9 points higher
        GlStateManager.translate(.5, 0.5, .5);

        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);

        GlStateManager.popMatrix();
	}

}
