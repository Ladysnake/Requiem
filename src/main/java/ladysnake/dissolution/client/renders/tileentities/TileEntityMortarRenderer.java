package ladysnake.dissolution.client.renders.tileentities;

import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import ladysnake.dissolution.common.tileentities.TileEntityMortar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileEntityMortarRenderer extends TileEntitySpecialRenderer<TileEntityMortar> {

    @Override
    public void render(TileEntityMortar te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GenericStackInventory<EnumPowderOres> powderInv = CapabilityGenericInventoryProvider.getInventory(te, EnumPowderOres.class);
        if (powderInv != null) {
            GlStateManager.disableCull();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);

            GlStateManager.translate(0.1, -0.38, 0.1);
            GlStateManager.scale(0.8, 1, 0.8);

            double amount = powderInv.getTotalAmount();
            double capacity = powderInv.getSlotLimit(0);
            GlStateManager.disableBlend();
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/sand");
            int i = te.getWorld().getLight(te.getPos());
            if (amount > 0)
                TileEntityCrucibleRenderer.renderLevel(amount, capacity, sprite, i, 0xFF, 0xFF, 0xFF, 0xFF);

            GlStateManager.popMatrix();

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
        }
        if (!te.getContent().isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate(0.5, 0.15, 0.4);
            GlStateManager.scale(0.8, 0.8, 0.8);
            GlStateManager.rotate(90, 1, 0, 0);
            Minecraft.getMinecraft().getRenderItem().renderItem(te.getContent(), ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }

    }
}
