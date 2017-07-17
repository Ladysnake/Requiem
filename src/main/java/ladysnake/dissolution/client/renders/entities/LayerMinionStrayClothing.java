package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.client.models.ModelMinionStray;
import ladysnake.dissolution.common.entity.minion.EntityMinionStray;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerMinionStrayClothing implements LayerRenderer<EntityMinionStray>
{
    private static final ResourceLocation STRAY_CLOTHES_TEXTURES = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
    private final RenderLivingBase<?> renderer;
    private final ModelMinionStray layerModel = new ModelMinionStray(0.25F, true);

    public LayerMinionStrayClothing(RenderLivingBase<?> p_i47183_1_)
    {
        this.renderer = p_i47183_1_;
    }

    public void doRenderLayer(EntityMinionStray entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.layerModel.setModelAttributes(this.renderer.getMainModel());
        this.layerModel.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderer.bindTexture(STRAY_CLOTHES_TEXTURES);
        this.layerModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    public boolean shouldCombineTextures()
    {
        return true;
    }


}