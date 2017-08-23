package ladysnake.dissolution.client.renders.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ladysnake.dissolution.common.handlers.PlayerInventoryListener;
import ladysnake.dissolution.common.items.ItemScythe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class LayerScythe implements LayerRenderer<EntityPlayer> {

	@Override
	public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 0.15);
				if(player.isSneaking()) {
					GlStateManager.translate(0F, 0.2F, 0F);
					GlStateManager.rotate(90F / (float) Math.PI, 1.0F, 0.0F, 0.0F);
				}
				ItemStack displayItem = PlayerInventoryListener.getItemToDisplay(player.getUniqueID());
				if(!displayItem.isEmpty() && !ItemStack.areItemStacksEqual(displayItem, player.getHeldItemMainhand()))
					Minecraft.getMinecraft().getRenderItem().renderItem(displayItem, ItemCameraTransforms.TransformType.NONE);
				GlStateManager.popMatrix();
				return;
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

}
