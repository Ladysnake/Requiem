package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.client.models.entities.ModelMinionSkeleton;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.entity.minion.EntityMinionStray;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class LayerMinionStrayClothing implements LayerRenderer<EntityMinionStray> {
    private static final ResourceLocation STRAY_CLOTHES_TEXTURES = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
    private final RenderLivingBase<AbstractMinion> renderer;
    private final ModelMinionSkeleton layerModel = new ModelMinionSkeleton(0.25F, true);

    public LayerMinionStrayClothing(RenderLivingBase<AbstractMinion> renderer) {
        this.renderer = renderer;
    }

    public void doRenderLayer(@Nonnull EntityMinionStray entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.layerModel.setModelAttributes(this.renderer.getMainModel());
        this.layerModel.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderer.bindTexture(STRAY_CLOTHES_TEXTURES);
        this.layerModel.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    public boolean shouldCombineTextures() {
        return true;
    }


}