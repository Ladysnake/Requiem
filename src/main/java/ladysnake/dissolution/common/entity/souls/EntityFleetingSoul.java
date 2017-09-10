package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityFleetingSoul extends AbstractSoul {

	private int soulAge;
	private int delayBeforeCanPickup;
	/**Spherical coordinates of this entity*/
	protected double theta, phi, r;
	/**Cartesian coordinates of this entity's target*/
	protected double xTarget, yTarget, zTarget;
	/**Cartesian coordinates of the spherical origin for the current path*/
	protected double xCenter, yCenter, zCenter;
	private EntityPlayer closestPlayer;
	
	public EntityFleetingSoul(World worldIn) {
		super(worldIn);
	}

	public EntityFleetingSoul(World worldIn, double x, double y, double z) {
		this(worldIn);
		this.setPosition(x, y, z);
	}

	public void onUpdate() {

		super.onUpdate();

		if (this.delayBeforeCanPickup > 0) {
			--this.delayBeforeCanPickup;
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.029999999329447746D;
		}

		this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D,
				this.posZ);
		double d0 = 8.0D;

		if (this.soulAge % 100 == 0) {
			if (this.closestPlayer == null || this.getDistanceSqToEntity(closestPlayer) > 2.0) {
				if((this.closestPlayer = this.world.getClosestPlayerToEntity(this, 1024.0)) != null) {
					this.xCenter = this.closestPlayer.posX + ((closestPlayer.posX - this.posX)/2.0);
					this.yCenter = this.closestPlayer.posY + ((closestPlayer.posY - this.posY)/2.0);
					this.zCenter = this.closestPlayer.posZ + ((closestPlayer.posZ - this.posZ)/2.0);
					this.r = Math.sqrt(Math.pow(this.posX - this.xCenter, 2) + Math.pow(this.posY - this.yCenter, 2) + Math.pow(this.posZ - this.zCenter, 2));
					this.theta = Math.acos((zCenter - this.posZ) / r);
					this.phi = Math.atan((yCenter - this.posY) / (xCenter - this.posX));
				}
			}
		}

		if (this.closestPlayer != null && this.closestPlayer.isSpectator()) {
			this.closestPlayer = null;
		}

		if (this.closestPlayer != null && !DissolutionInventoryHelper.findItem(closestPlayer, ModItems.HALITE).isEmpty()) {
			this.r += 0;
			this.phi += (Math.PI / 180.0);
			this.theta += (Math.PI / 180.0);
			double newPosX = this.r * Math.sin(this.theta) * Math.cos(this.phi);
			double newPosY = this.r * Math.sin(this.theta) * Math.sin(this.phi);
			double newPosZ = this.r * Math.cos(this.theta);
			this.motionX = (newPosX + this.xCenter) - this.posX;
			this.motionY = (newPosY + this.yCenter) - this.posY;
			this.motionZ = (newPosZ + this.zCenter) - this.posZ;
			System.out.println("movX=" + this.motionX + " movY=" + this.motionY + " movZ=" + this.motionZ);
			/*
			double d1 = (this.closestPlayer.posX - this.posX) / 8.0D;
			double d2 = (this.closestPlayer.posY + (double) this.closestPlayer.getEyeHeight() / 2.0D - this.posY)
					/ 8.0D;
			double d3 = (this.closestPlayer.posZ - this.posZ) / 8.0D;
			double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
			double d5 = 1.0D - d4;

			if (d5 > 0.0D) {
				d5 = d5 * d5;
				this.motionX += d1 / d4 * d5 * 0.1D;
				this.motionY += d2 / d4 * d5 * 0.1D;
				this.motionZ += d3 / d4 * d5 * 0.1D;
			}
			*/
		}

		if(!world.isRemote)
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

		if (this.onGround) {
			this.motionY *= -0.8999999761581421D;
		}

		++this.soulAge;

		if (this.soulAge >= 6000) {
			this.setDead();
		}
		
		if(this.world.isRemote) {
			for (double i = 0; i < 9; i ++){
				double coeff = i/9.0;
				Dissolution.proxy.spawnParticle(getEntityWorld(), 
						(float)(prevPosX+(posX-prevPosX)*coeff), (float)(prevPosY+(posY-prevPosY)*coeff), (float)(prevPosZ+(posZ-prevPosZ)*coeff), 	//position
						0.0125f*(rand.nextFloat()-0.5f), 0.0125f*(rand.nextFloat()-0.5f), 0.0125f*(rand.nextFloat()-0.5f), 	//motion
						255, 64, 16, 255, 	//color
						2.0f, 24);
			}
		}
	}

}
