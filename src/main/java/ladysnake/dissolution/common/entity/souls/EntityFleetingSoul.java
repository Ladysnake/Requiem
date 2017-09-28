package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.api.Soul;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.items.ItemSoulInABottle;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityFleetingSoul extends AbstractSoul {
	
	private int delayBeforeCanPickup = 10;
	private int targetChangeCooldown = 0;
	private EntityPlayer closestPlayer;

	public EntityFleetingSoul(World worldIn) {
		super(worldIn);
	}
	
	public EntityFleetingSoul(World worldIn, double x, double y, double z) {
		this(worldIn);
		this.setPosition(x, y, z);
	}
	
	public EntityFleetingSoul(World worldIn, double x, double y, double z, Soul soulIn) {
		super(worldIn, soulIn);
		this.setPosition(x, y, z);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.delayBeforeCanPickup > 0) {
			--this.delayBeforeCanPickup;
		}

		if (this.soulAge >= 6000) {
			world.removeEntity(this);
		}
		
		if (!this.world.isRemote && !this.isDead) {
			
			this.targetChangeCooldown -= (this.getPositionVector().squareDistanceTo(lastTickPosX, lastTickPosY, lastTickPosZ) < 0.0125) ? 10 : 1;
			
			if((xTarget == 0 && yTarget == 0 && zTarget == 0) || this.getPosition().distanceSq(xTarget, yTarget, zTarget) < 9 || targetChangeCooldown <= 0) {
				this.xTarget = this.posX + rand.nextGaussian() * 10;
				this.yTarget = Math.max(this.posY + rand.nextGaussian() * 10, (this.soulAge / 20.0));
				this.zTarget = this.posZ + rand.nextGaussian() * 10;
				targetChangeCooldown = rand.nextInt() % 200;
			}

			if (this.soulAge % 100 == 0)
				if (this.closestPlayer == null || this.getDistanceSqToEntity(closestPlayer) > 1024.0)
					this.closestPlayer = this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 32.0, 
							player -> !((EntityPlayer)player).isSpectator() && !DissolutionInventoryHelper.findItem((EntityPlayer) player, ModItems.HALITE).isEmpty());

			if (this.closestPlayer != null && this.closestPlayer.isSpectator()) {
				this.closestPlayer = null;
			}
			
			double targetX = this.closestPlayer != null ? closestPlayer.posX : this.xTarget;
			double targetY = this.closestPlayer != null ? closestPlayer.posY : this.yTarget;
			double targetZ = this.closestPlayer != null ? closestPlayer.posZ : this.zTarget;
			Vec3d targetVector = new Vec3d(targetX-posX,targetY-posY,targetZ-posZ);
			double length = targetVector.lengthVector();
			targetVector = targetVector.scale(0.3/length);
			double weight  = 0;
			if (length <= 3){
				weight = 0.9*((3.0-length)/3.0);
			}
			motionX = (0.9-weight)*motionX+(0.1+weight)*targetVector.x;
			motionY = (0.9-weight)*motionY+(0.1+weight)*targetVector.y;
			motionZ = (0.9-weight)*motionZ+(0.1+weight)*targetVector.z;
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
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
	
	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {
		ItemStack bottle = DissolutionInventoryHelper.findItem(entityIn, Items.GLASS_BOTTLE);
		if(!world.isRemote && !bottle.isEmpty() && this.delayBeforeCanPickup <= 0) {
			bottle.shrink(1);
			entityIn.addItemStackToInventory(ItemSoulInABottle.newTypedSoulBottle(this.soul.getType()));
			this.setDead();
		}
	}

}
