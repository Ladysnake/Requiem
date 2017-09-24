package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.api.Soul;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractSoul extends Entity {
	
	protected Soul soul;
	protected int soulAge;
	protected double xTarget, yTarget, zTarget;
	
	public AbstractSoul(World worldIn) {
		this(worldIn, Soul.UNDEFINED);
	}
	
	public AbstractSoul(World worldIn, Soul soulIn) {
		super(worldIn);
		this.setSize(0.5F, 0.5F);
		this.setEntityInvulnerable(true);
		this.setNoGravity(true);
		this.rotationYaw = (float) (Math.random() * 360.0D);
		this.motionX = (Math.random() * 0.2 - 0.1) * 2.0F;
		this.motionY = (Math.random() * 0.2) * 2.0F;
		this.motionZ = (Math.random() * 0.2 - 0.1) * 2.0F;
		this.soul = soulIn;
	}
	
	public BlockPos getTargetPosition() {
		return new BlockPos(this.xTarget, this.yTarget + 0.5, this.zTarget);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		++this.soulAge;
		
		if(this.world.isRemote) {
			spawnParticles();
		}
	}
	
	@SideOnly(Side.CLIENT)
	protected abstract void spawnParticles();

	@Override
	protected void entityInit() {}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk
	 * on. used for spiders and wolves to prevent them from trampling crops
	 */
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.soul = new Soul(compound.getCompoundTag("soul"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setTag("soul", soul.writeToNBT());
	}

}
