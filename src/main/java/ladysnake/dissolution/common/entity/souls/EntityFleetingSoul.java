package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.common.Dissolution;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityFleetingSoul extends AbstractSoul {

	public int xpOrbAge;
	public int delayBeforeCanPickup;
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

		if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA) {
			this.motionY = 0.20000000298023224D;
			this.motionX = (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
			this.motionZ = (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
			this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
		}

		this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D,
				this.posZ);
		double d0 = 8.0D;

		if (this.xpOrbAge % 100 == 0) {
			if (this.closestPlayer == null || this.closestPlayer.getDistanceSqToEntity(this) > 64.0D) {
				this.closestPlayer = this.world.getClosestPlayerToEntity(this, 8.0D);
			}

		}

		if (this.closestPlayer != null && this.closestPlayer.isSpectator()) {
			this.closestPlayer = null;
		}

		if (this.closestPlayer != null) {
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
		}

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		float f = 0.98F;

		if (this.onGround) {
			f = this.world
					.getBlockState(new BlockPos(MathHelper.floor(this.posX),
							MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ)))
					.getBlock().slipperiness * 0.98F;
		}

		this.motionX *= (double) f;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= (double) f;

		if (this.onGround) {
			this.motionY *= -0.8999999761581421D;
		}

		++this.xpOrbAge;

		if (this.xpOrbAge >= 6000) {
			this.setDead();
		}
		
		if(this.world.isRemote)
			for (double i = 0; i < 9; i ++){
				double coeff = i/9.0;
				Dissolution.proxy.spawnParticle(getEntityWorld(), (float)(prevPosX+(posX-prevPosX)*coeff), (float)(prevPosY+(posY-prevPosY)*coeff), (float)(prevPosZ+(posZ-prevPosZ)*coeff), 0.0125f*(rand.nextFloat()-0.5f), 0.0125f*(rand.nextFloat()-0.5f), 0.0125f*(rand.nextFloat()-0.5f), 255, 64, 16, 1.0f, 2.0f, 24);
			}
	}

}
