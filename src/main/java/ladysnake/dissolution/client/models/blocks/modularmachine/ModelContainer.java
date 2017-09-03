package ladysnake.dissolution.client.models.blocks.modularmachine;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * container by Unknown
 */
@SideOnly(Side.CLIENT)
public class ModelContainer extends ModelBase {
    public ModelRenderer container;
    public ModelRenderer support;
    public ModelRenderer plug;
    public ModelRenderer support2;
    public ModelRenderer containerTop;

    public ModelContainer() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.container = new ModelRenderer(this, 0, 43);
        this.container.setRotationPoint(-5.5F, 12.0F, -5.5F);
        this.container.addBox(0.0F, 0.0F, 0.0F, 11, 10, 11);
        this.support = new ModelRenderer(this, 0, 0);
        this.support.setRotationPoint(-3.0F, 11.5F, -6.0F);
        this.support.addBox(0.0F, 0.0F, 0.0F, 6, 11, 12);
        this.plug = new ModelRenderer(this, 50, 12);
        this.plug.setRotationPoint(-2.0F, 14.0F, -8.0F);
        this.plug.addBox(0.0F, 0.0F, 0.0F, 4, 4, 3);
        this.support2 = new ModelRenderer(this, 28, 24);
        this.support2.setRotationPoint(-4.0F, 18.0F, 3.0F);
        this.support2.addBox(-2.0F, -6.5F, -6.0F, 12, 11, 6);
        this.containerTop = new ModelRenderer(this, 28, 0);
        this.containerTop.setRotationPoint(-2.5F, 17.5F, 1.5F);
        this.containerTop.addBox(-2.0F, -6.5F, -6.0F, 9, 1, 9);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
        this.container.render(scale);
        GlStateManager.disableBlend();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
        this.support.render(scale);
        GlStateManager.disableBlend();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
        this.plug.render(scale);
        GlStateManager.disableBlend();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
        this.support2.render(scale);
        GlStateManager.disableBlend();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.0F);
        this.containerTop.render(scale);
        GlStateManager.disableBlend();
    }

    public void setRotationAngles(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
