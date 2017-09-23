package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityOrbitingSoul extends AbstractSoul {

	private int soulAge;
	private int delayBeforeCanPickup;
	/**Spherical coordinates of this entity*/
	protected double theta, phi, r;
	/**Cartesian coordinates of the spherical origin for the current path*/
	protected double xCenter, yCenter, zCenter;
	/**Motion in spherical coordinates*/
	protected double motionTheta, motionPhi, motionR;
	private EntityPlayer closestPlayer;
	
	public EntityOrbitingSoul(World worldIn) {
		super(worldIn);
		this.noClip = true;
	}

	public EntityOrbitingSoul(World worldIn, double x, double y, double z) {
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

//		this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
		double d0 = 8.0D;

		if (this.soulAge % 100 == 0) {
			if (this.closestPlayer == null || this.getDistanceSqToEntity(closestPlayer) > 1024.0) {
				if((this.closestPlayer = this.world.getClosestPlayerToEntity(this, 1024.0)) != null) {
					this.xCenter = this.closestPlayer.posX;// + ((closestPlayer.posX - this.posX)/2.0);
					this.yCenter = this.closestPlayer.posY;// + ((closestPlayer.posY - this.posY)/2.0);
					this.zCenter = this.closestPlayer.posZ;// + ((closestPlayer.posZ - this.posZ)/2.0);
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
			this.motionR = 0;
			this.motionPhi = (Math.PI / 180.0) * 2.0;
			this.motionTheta = (Math.PI / 180.0) * 2.0;
		} else if(!world.isRemote){
			this.motionR = 0;
			this.motionPhi += (Math.PI / 180.0) * rand.nextGaussian() * 5;
			this.motionTheta += (Math.PI / 180.0) * rand.nextGaussian() * 5;
		}
		
		this.r += motionR;
		this.phi += motionPhi;
		this.theta += motionTheta;
		double newPosX = this.r * Math.sin(this.theta) * Math.cos(this.phi);
		double newPosY = this.r * Math.sin(this.theta) * Math.sin(this.phi);
		double newPosZ = this.r * Math.cos(this.theta);
		this.motionX = (newPosX + this.xCenter) - this.posX;
		this.motionY = (newPosY + this.yCenter) - this.posY;
		this.motionZ = (newPosZ + this.zCenter) - this.posZ;

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

		if (this.onGround) {
			this.motionY *= -0.8999999761581421D;
		}

		++this.soulAge;

		if (this.soulAge >= 6000) {
			this.setDead();
		}
		
		if(this.world.isRemote) {
			spawnParticles();
		}
	}
	
	@SideOnly(Side.CLIENT)
	protected void spawnParticles() {
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
