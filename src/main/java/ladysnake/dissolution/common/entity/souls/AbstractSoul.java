package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.api.ISoulHandler;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class AbstractSoul extends Entity {
	
	private ISoulHandler soulInventory;
	
	public AbstractSoul(World worldIn) {
		super(worldIn);
		this.setSize(0.5F, 0.5F);
		this.setEntityInvulnerable(true);
		this.setNoGravity(true);
		this.rotationYaw = (float) (Math.random() * 360.0D);
		this.motionX = (Math.random() * 0.2 - 0.1) * 2.0F;
		this.motionY = (Math.random() * 0.2) * 2.0F;
		this.motionZ = (Math.random() * 0.2 - 0.1) * 2.0F;
		this.soulInventory = new CapabilitySoulHandler.DefaultSoulInventoryHandler();
	}

	@Override
	protected void entityInit() {
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk
	 * on. used for spiders and wolves to prevent them from trampling crops
	 */
	protected boolean canTriggerWalking() {
		return false;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilitySoulHandler.CAPABILITY_SOUL_INVENTORY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilitySoulHandler.CAPABILITY_SOUL_INVENTORY)
			return (T) this.soulInventory;
		return super.getCapability(capability, facing);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub

	}

}
