package ladysnake.dissolution.client.models.entities;

import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class ModelPlayerCorpse extends ModelBiped {

    public ModelRenderer bipedLeftArmwear;
    public ModelRenderer bipedRightArmwear;
    public ModelRenderer bipedLeftLegwear;
    public ModelRenderer bipedRightLegwear;
    public ModelRenderer bipedBodyWear;
    private final ModelRenderer bipedCape;
    private final ModelRenderer bipedDeadmau5Head;
    private final boolean smallArms;

    public ModelPlayerCorpse(float modelSize, boolean smallArmsIn) {
        super(modelSize, 0.0F, 64, 64);
        this.smallArms = smallArmsIn;
        this.bipedDeadmau5Head = new ModelRenderer(this, 24, 0);
        this.bipedDeadmau5Head.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, modelSize);
        this.bipedCape = new ModelRenderer(this, 0, 0);
        this.bipedCape.setTextureSize(64, 32);
        this.bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, modelSize);

        if (smallArmsIn) {
            this.bipedLeftArm = new ModelRenderer(this, 32, 48);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
            this.bipedRightArm = new ModelRenderer(this, 40, 16);
            this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
            this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, modelSize + 0.25F);
            this.bipedLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);
            this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, modelSize + 0.25F);
            this.bipedRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);
        } else {
            this.bipedLeftArm = new ModelRenderer(this, 32, 48);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
            this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
            this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);
        }

        this.bipedLeftLeg = new ModelRenderer(this, 16, 48);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        this.bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedRightLegwear = new ModelRenderer(this, 0, 32);
        this.bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize + 0.25F);
        this.bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedBodyWear = new ModelRenderer(this, 16, 32);
        this.bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize + 0.25F);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.pushMatrix();

        if (this.isChild) {
            float f = 2.0F;
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            this.bipedLeftLegwear.render(scale);
            this.bipedRightLegwear.render(scale);
            this.bipedLeftArmwear.render(scale);
            this.bipedRightArmwear.render(scale);
            this.bipedBodyWear.render(scale);
        } else {
            if (entityIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            this.bipedLeftLegwear.render(scale);
            this.bipedRightLegwear.render(scale);
            this.bipedLeftArmwear.render(scale);
            this.bipedRightArmwear.render(scale);
            this.bipedBodyWear.render(scale);
        }

        GlStateManager.popMatrix();
    }

    public void renderDeadmau5Head(float scale) {
        copyModelAngles(this.bipedHead, this.bipedDeadmau5Head);
        this.bipedDeadmau5Head.rotationPointX = 0.0F;
        this.bipedDeadmau5Head.rotationPointY = 0.0F;
        this.bipedDeadmau5Head.render(scale);
    }

    public void renderCape(float scale) {
        this.bipedCape.render(scale);
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        if (((EntityPlayerCorpse) entityIn).isInert()) {

            ((EntityPlayerCorpse) entityIn).isAIDisabled();
            // right arm
            this.bipedRightArm.rotateAngleX = 1.5F;
            this.bipedRightArm.rotateAngleY = -0.1F;
            this.bipedRightArm.rotateAngleZ = 3.0F;
            this.bipedRightArm.rotationPointX = 3.5F;
            this.bipedRightArm.rotationPointY = 24.0F;
            this.bipedRightArm.rotationPointZ = 1.0F;
            // left arm
            this.bipedLeftArm.rotateAngleX = 1.5F;
            this.bipedLeftArm.rotateAngleY = 0.9F;
            this.bipedLeftArm.rotateAngleZ = 3.0F;
            this.bipedLeftArm.rotationPointX = -7.0F;
            this.bipedLeftArm.rotationPointY = 23.0F;
            this.bipedLeftArm.rotationPointZ = 1.0F;
            // right leg
            this.bipedRightLeg.rotateAngleX = 1.2F;
            this.bipedRightLeg.rotateAngleY = -0.1F;
            this.bipedRightLeg.rotateAngleZ = 1.6F;
            this.bipedRightLeg.rotationPointX = -4.5F;
            this.bipedRightLeg.rotationPointY = 22.1F;
            this.bipedRightLeg.rotationPointZ = 10.0F;
            // left leg
            this.bipedLeftLeg.rotateAngleX = 1.9F;
            this.bipedLeftLeg.rotateAngleY = 0.0F;
            this.bipedLeftLeg.rotateAngleZ = 1.5F;
            this.bipedLeftLeg.rotationPointX = -1.0F;
            this.bipedLeftLeg.rotationPointY = 22.1F;
            this.bipedLeftLeg.rotationPointZ = 10.0F;
            // body
            this.bipedBody.rotateAngleX = 1.6F;
            this.bipedBody.rotateAngleY = 0.0F;
            this.bipedBody.rotateAngleZ = 0.0F;
            this.bipedBody.rotationPointX = -2.2F;
            this.bipedBody.rotationPointY = 22.0F;
            this.bipedBody.rotationPointZ = 0.0F;
            // head
            this.bipedHead.rotateAngleX = 1.6F; // + (float) (-0.1F+ Math.cos(ageInTicks/4));
            this.bipedHead.rotateAngleY = 0.0F; // + (float) (-0.1F+ Math.sin(ageInTicks/4));
            this.bipedHead.rotateAngleZ = 4.0F;
            this.bipedHead.rotationPointX = -2.2F;
            this.bipedHead.rotationPointY = 22.0F;
            this.bipedHead.rotationPointZ = 0.0F;

            this.bipedHeadwear.rotateAngleX = 1.6F;
            this.bipedHeadwear.rotateAngleY = 0.0F;
            this.bipedHeadwear.rotateAngleZ = 4.0F;
            this.bipedHeadwear.rotationPointX = -2.2F;
            this.bipedHeadwear.rotationPointY = 22.0F;
            this.bipedHeadwear.rotationPointZ = 0.0F;
        } else {
            float f = MathHelper.sin(this.swingProgress * (float) Math.PI);
            float f1 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float) Math.PI);
            this.bipedRightArm.rotateAngleZ = 0.0F;
            this.bipedLeftArm.rotateAngleZ = 0.0F;
            this.bipedRightArm.rotateAngleY = -(0.1F - f * 0.6F);
            this.bipedLeftArm.rotateAngleY = 0.1F - f * 0.6F;
            float f2 = -(float) Math.PI / 2.25F;
            this.bipedRightArm.rotateAngleX = f2;
            this.bipedLeftArm.rotateAngleX = f2;
            this.bipedRightArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
            this.bipedLeftArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
            this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
            this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

            this.bipedRightArm.rotationPointY = 2.25F;
            this.bipedLeftArm.rotationPointY = 2.25F;

            this.bipedBody.rotationPointX = -0.0F;
            this.bipedBody.rotationPointY = 0.0F;
            this.bipedBody.rotationPointZ = 0.0F;

            this.bipedLeftArm.rotationPointX = 5.0F;
            this.bipedLeftArm.rotationPointY = 2.25F;
            this.bipedLeftArm.rotationPointZ = 0.0F;

            this.bipedLeftLeg.rotationPointX = -2.0F;
            this.bipedLeftLeg.rotationPointY = 12.0F;
            this.bipedLeftLeg.rotationPointZ = 0.0F;

            this.bipedRightLeg.rotationPointX = 2.0F;
            this.bipedRightLeg.rotationPointY = 12.0F;
            this.bipedRightLeg.rotationPointZ = 0.0F;

            this.bipedHead.rotateAngleX = 0.0F;
            this.bipedHead.rotateAngleY = 0.0F;
            this.bipedHead.rotateAngleZ = 0.0F;
            this.bipedHead.rotationPointX = 0.0F;
            this.bipedHead.rotationPointY = 0.0F;
            this.bipedHead.rotationPointZ = 0.0F;

            this.bipedHeadwear.rotateAngleX = 0.0F;
            this.bipedHeadwear.rotateAngleY = 0.0F;
            this.bipedHeadwear.rotateAngleZ = 0.0F;
            this.bipedHeadwear.rotationPointX = 0.0F;
            this.bipedHeadwear.rotationPointY = 0.0F;
            this.bipedHeadwear.rotationPointZ = 0.0F;
        }

        if (entityIn.isSneaking()) {
            this.bipedCape.rotationPointY = 2.0F;
        } else {
            this.bipedCape.rotationPointY = 0.0F;
        }

        copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);
        copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
        copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
        copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
        copyModelAngles(this.bipedBody, this.bipedBodyWear);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.bipedLeftArmwear.showModel = visible;
        this.bipedRightArmwear.showModel = visible;
        this.bipedLeftLegwear.showModel = visible;
        this.bipedRightLegwear.showModel = visible;
        this.bipedBodyWear.showModel = visible;
        this.bipedCape.showModel = visible;
        this.bipedDeadmau5Head.showModel = visible;
    }

    public void postRenderArm(float scale, EnumHandSide side) {
        ModelRenderer modelrenderer = this.getArmForSide(side);

        if (this.smallArms) {
            float f = 0.5F * (float) (side == EnumHandSide.RIGHT ? 1 : -1);
            modelrenderer.rotationPointX += f;
            modelrenderer.postRender(scale);
            modelrenderer.rotationPointX -= f;
        } else {
            modelrenderer.postRender(scale);
        }
    }

}
