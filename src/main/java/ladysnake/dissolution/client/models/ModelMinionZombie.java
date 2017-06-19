package ladysnake.dissolution.client.models;

import ladysnake.dissolution.common.entity.EntityMinion;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelMinionZombie extends ModelZombie {
	public ModelMinionZombie() {
		this(0.0F, false);
	}

	public ModelMinionZombie(float modelSize, boolean show) {
		super(modelSize, show);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		
		if (!((EntityMinion) entityIn).isCorpse()) {
			super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
	        boolean flag = entityIn instanceof EntityZombie && ((EntityZombie)entityIn).isArmsRaised();
	        float f = MathHelper.sin(this.swingProgress * (float)Math.PI);
	        float f1 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float)Math.PI);
	        this.bipedRightArm.rotateAngleZ = 0.0F;
	        this.bipedLeftArm.rotateAngleZ = 0.0F;
	        this.bipedRightArm.rotateAngleY = -(0.1F - f * 0.6F);
	        this.bipedLeftArm.rotateAngleY = 0.1F - f * 0.6F;
	        float f2 = -(float)Math.PI / (flag ? 1.5F : 2.25F);
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
		
		else {
	        
	        ((EntityMinion) entityIn).isAIDisabled();
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
			
			this.bipedHeadwear.rotateAngleX = 1.6F;
			this.bipedHeadwear.rotateAngleY = 0.0F;
			this.bipedHeadwear.rotateAngleZ = 4.0F;
			this.bipedHeadwear.rotationPointX = -2.2F;
			this.bipedHeadwear.rotationPointY = 22.0F;
			this.bipedHeadwear.rotationPointZ = 0.0F;
		}
	}
}
