package ladysnake.tartaros.client.model;

import ladysnake.tartaros.common.entity.EntityMinionZombie;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelMinionZombie extends ModelBiped {
	public ModelMinionZombie() {
		this(0.0F, false);
	}

	public ModelMinionZombie(float modelSize, boolean show) {
		super(modelSize, 0.0F, 64, show ? 32 : 64);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		if (((EntityMinionZombie)entityIn).isCorpse()) {
			System.out.println("is dead");
			super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
			boolean flag = entityIn instanceof EntityZombie && ((EntityZombie) entityIn).isArmsRaised();
			float f = MathHelper.sin(this.swingProgress * (float) Math.PI);
			float f1 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float) Math.PI);
			this.bipedRightArm.rotateAngleZ = 1.0F;
			this.bipedLeftArm.rotateAngleZ = 0.0F;
			this.bipedRightArm.rotateAngleY = -(0.1F - f * 0.6F);
			this.bipedLeftArm.rotateAngleY = 0.1F - f * 0.6F;
			float f2 = -(float) Math.PI / (flag ? 1.5F : 2.25F);
			this.bipedRightArm.rotateAngleX = f2;
			this.bipedLeftArm.rotateAngleX = f2;
			this.bipedRightArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
			this.bipedLeftArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
			this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
			this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		}
		else {
			System.out.println("is not dead");
			super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
			boolean flag = entityIn instanceof EntityZombie && ((EntityZombie) entityIn).isArmsRaised();
			float f = MathHelper.sin(this.swingProgress * (float) Math.PI);
			float f1 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float) Math.PI);
			this.bipedRightArm.rotateAngleZ = 0.0F;
			this.bipedLeftArm.rotateAngleZ = 0.0F;
			this.bipedRightArm.rotateAngleY = -(0.1F - f * 0.6F);
			this.bipedLeftArm.rotateAngleY = 0.1F - f * 0.6F;
			float f2 = -(float) Math.PI / (flag ? 1.5F : 2.25F);
			this.bipedRightArm.rotateAngleX = f2;
			this.bipedLeftArm.rotateAngleX = f2;
			this.bipedRightArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
			this.bipedLeftArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
			this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
			this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
			this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		}
	}
}
