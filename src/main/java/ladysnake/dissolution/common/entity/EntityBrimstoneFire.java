package ladysnake.dissolution.common.entity;

import java.util.List;

import com.google.common.base.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityBrimstoneFire extends Entity {
	
	private static DataParameter<Optional<BlockPos>> TARGET_FIRE = EntityDataManager.<Optional<BlockPos>>createKey(EntityBrimstoneFire.class, DataSerializers.OPTIONAL_BLOCK_POS);
	private int ticksFired;
	public static final int MAX_FIRE_TIME = 10;

	public EntityBrimstoneFire(World worldIn) {
		super(worldIn);
        this.setSize(0.25F, 0.25F);
        this.ticksFired = -1;
        this.ignoreFrustumCheck = true;
        this.updateBlocked = true;
	}
	
	public void fire() {
		System.out.println(this.getTarget());
		this.ticksFired = 0;
		//this.setDead();
	}
	
	/**
	 * Updates the brimstone attack
	 * @return true if the attack has ended
	 */
	public boolean updateFiring() {
		if(this.ticksFired >= 0) {
			System.out.println(this);
			if(this.ticksFired++ > MAX_FIRE_TIME) {
				this.ticksFired = -1;
				this.setDead();
				return true;
			}
		}
		return false;
	}
	
	public void setTarget(BlockPos pos) {
		this.getDataManager().set(TARGET_FIRE, Optional.of(pos));
//		System.out.println(pos + " " + getDataManager().get(TARGET_FIRE));
	}
	
	public BlockPos getTarget() {
		if(this.getDataManager().get(TARGET_FIRE).isPresent())
			return this.getDataManager().get(TARGET_FIRE).get();
		return BlockPos.ORIGIN;
	}

	@Override
	protected void entityInit() {
		this.getDataManager().register(TARGET_FIRE, Optional.absent());
	}

	@Override
	public String toString() {
		return "EntityBrimstoneFire [TARGET_FIRE=" + this.getTarget() + ", ticksFired=" + ticksFired + "]";
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {}
	
}
