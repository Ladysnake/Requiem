package ladysnake.dissolution.client.models;

import ladysnake.dissolution.common.entity.minion.EntityMinionSkeleton;
import ladysnake.dissolution.common.entity.minion.EntityMinionStray;
import ladysnake.dissolution.common.entity.minion.EntityMinionZombie;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelMinionStray extends ModelBiped
{
    public ModelMinionStray()
    {
        this(0.0F, false);
    }

    public ModelMinionStray(float modelSize, boolean show)
    {
        super(modelSize, 0.0F, 64, 32);

        if (!show)
        {
            this.bipedRightArm = new ModelRenderer(this, 40, 16);
            this.bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, modelSize);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
            this.bipedLeftArm = new ModelRenderer(this, 40, 16);
            this.bipedLeftArm.mirror = true;
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, modelSize);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedRightLeg = new ModelRenderer(this, 0, 16);
            this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, modelSize);
            this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
            this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
            this.bipedLeftLeg.mirror = true;
            this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, modelSize);
            this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        }
    }



    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        ItemStack itemstack = ((EntityLivingBase)entityIn).getHeldItemMainhand();

        if (!((EntityMinionStray) entityIn).isCorpse()) {
        	
            float f = MathHelper.sin(this.swingProgress * (float)Math.PI);
            float f1 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float)Math.PI);
            this.bipedRightArm.rotateAngleZ = 0.0F;
            this.bipedLeftArm.rotateAngleZ = 0.0F;
            this.bipedRightArm.rotateAngleY = -(0.1F - f * 0.6F);
            this.bipedLeftArm.rotateAngleY = 0.1F - f * 0.6F;
            this.bipedRightArm.rotateAngleX = -((float)Math.PI / 2F);
            this.bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F);
            this.bipedRightArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
            this.bipedLeftArm.rotateAngleX -= f * 1.2F - f1 * 0.4F;
            this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
            this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
            
            this.bipedRightArm.rotateAngleX = 6F + MathHelper.cos(ageInTicks * 0.2F) * 0.5F + 0.05F;
            this.bipedLeftArm.rotateAngleX = 6F + MathHelper.sin(ageInTicks * 0.2F) * 0.5F + 0.05F;
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
        }
        else {
        	((EntityMinionStray) entityIn).isAIDisabled();		
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
			this.bipedHead.rotateAngleX = 1.6F;
			this.bipedHead.rotateAngleY = 0.0F;
			this.bipedHead.rotateAngleZ = 4.0F;
			this.bipedHead.rotationPointX = -2.2F;
			this.bipedHead.rotationPointY = 22.0F;
			this.bipedHead.rotationPointZ = 0.0F;
        }
    }

    public void postRenderArm(float scale, EnumHandSide side)
    {
        float f = side == EnumHandSide.RIGHT ? 1.0F : -1.0F;
        ModelRenderer modelrenderer = this.getArmForSide(side);
        modelrenderer.rotationPointX += f;
        modelrenderer.postRender(scale);
        modelrenderer.rotationPointX -= f;
    }
}