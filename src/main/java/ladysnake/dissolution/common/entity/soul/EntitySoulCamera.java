package ladysnake.dissolution.common.entity.soul;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySoulCamera extends AbstractSoul {
	
	double velocityX = 0, velocityY = 0, velocityZ = 0;
	BlockPos dest;

	public EntitySoulCamera(World worldIn) {
		super(worldIn);
	}
	
	public EntitySoulCamera(EntityPlayer playerIn) {
		this(playerIn.world);
		this.setOwnerId(playerIn.getUniqueID());
		this.posX = playerIn.posX;
		this.posY = playerIn.posY;
		this.posZ = playerIn.posZ;
		System.out.println("[EntitySoulCamera.<init>] camera position : " + this.getPosition() + " player position : " + playerIn.getPosition());
		((EntityPlayerMP)playerIn).connection.sendPacket(new SPacketCamera(this));
	}
	
	public void setDest(BlockPos dest) {
		this.dest = dest;
	}
	
	@Override
	public void onUpdate() {
		this.noClip = true;
		if(dest != null)// && !world.isRemote)
		{
			Vec3d motion = new Vec3d(
					Math.signum(dest.getX() - posX), 
					Math.signum(dest.getY() - posY), 
					Math.signum(dest.getY() - posY)).normalize().scale(0.1);
			posX += motion.x;
			posY += motion.y;
			posZ += motion.z;
			EntityLivingBase owner = this.getOwner();
			owner.setPositionAndUpdate(posX, posY, posZ);
		}
		super.onUpdate();
		this.noClip = false;
	}
	
	protected void updateAITasks()
    {

        {
            double d0 = this.dest.getX() + 0.5D - this.posX;
            double d1 = this.dest.getY() + 0.1D - this.posY;
            double d2 = this.dest.getZ() + 0.5D - this.posZ;
            this.motionX += (Math.signum(d0) * 0.5D - this.motionX) * 0.10000000149011612D;
            this.motionY += (Math.signum(d1) * 0.699999988079071D - this.motionY) * 0.10000000149011612D;
            this.motionZ += (Math.signum(d2) * 0.5D - this.motionZ) * 0.10000000149011612D;
            float f = (float)(MathHelper.atan2(this.motionZ, this.motionX) * (180D / Math.PI)) - 90.0F;
            float f1 = MathHelper.wrapDegrees(f - this.rotationYaw);
            // this.moveForward = 0.5F;
            this.rotationYaw += f1;
        }
    }
	
}
