package ladysnake.dissolution.common.entity.soul;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntitySoulCamera extends AbstractSoul {
	
	double velocityX = 0, velocityY = 0, velocityZ = 0;
	BlockPos dest;

	public EntitySoulCamera(World worldIn) {
		super(worldIn);
	}
	
	public EntitySoulCamera(EntityPlayerMP playerIn) {
		this(playerIn.world);
		this.setOwnerId(playerIn.getUniqueID());
		this.posX = playerIn.posX;
		this.posY = playerIn.posY;
		this.posZ = playerIn.posZ;
		System.out.println("[EntitySoulCamera.<init>] camera position : " + this.getPosition() + " player position : " + playerIn.getPosition());
		//playerIn.connection.sendPacket(new SPacketCamera(this));
	}
	
	public void setDest(BlockPos dest) {
		this.dest = dest;
	}
	
	@Override
	public void onUpdate() {
		this.noClip = true;
		super.onUpdate();
		this.noClip = false;
		if(!world.isRemote && this.getPassengers().isEmpty() && this.getOwner() != null && false)
			this.getOwner().startRiding(this);
		System.out.println(this);
		if(this.ticksExisted > 500) {
			this.setDead();
		}
	}
	
	@Override
	public void setVelocity(double x, double y, double z) {
		super.setVelocity(x, y, z);
        this.velocityX = this.motionX;
        this.velocityY = this.motionY;
        this.velocityZ = this.motionZ;
	}
	
	@Override
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
			int posRotationIncrements, boolean teleport) {
		super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
		this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
	}

}
