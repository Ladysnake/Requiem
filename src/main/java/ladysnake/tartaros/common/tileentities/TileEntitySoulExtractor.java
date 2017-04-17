package ladysnake.tartaros.common.tileentities;

import java.util.Random;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;

public class TileEntitySoulExtractor extends TileEntity implements ITickable {
	
	public static final int MAX_SOULSAND = 128;
	private int soulSandCount, soulCount;
	private short processElapsed;

	@Override
	public void update() {
		if(this.world.isRemote) return;
		if(this.isEmpty()) {
			this.processElapsed = 0;
			return;
		}
		++processElapsed;
		if(processElapsed%200 == 0) {
			soulSandCount--;
			Random rand = new Random();
			soulCount+= rand.nextInt(2);
		}
	}
	
	public boolean isEmpty() {
		return soulSandCount == 0;
	}
	
	@Override
    public NBTTagCompound getUpdateTag() {
    	return writeToNBT(new NBTTagCompound());
    }
	
	@Override
    public SPacketUpdateTileEntity getUpdatePacket() {
    	NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.soulSandCount = compound.getInteger("SoulSandCount");
        this.soulCount = compound.getInteger("SoulCount");
        this.processElapsed = compound.getShort("processElapsed");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("SoulSandCount", this.soulSandCount);
        compound.setInteger("SoulCount", this.soulCount);
        compound.setShort("ProcessElapsed", this.processElapsed);

        return compound;
    }
	
	/**
	 * Adds soulsand to the count.
	 * @param count The quantity that should be added.
	 * @return The remaining quantity.
	 */
	public int addSoulSand(int count) {
		if(soulSandCount + count > MAX_SOULSAND){
			count -= MAX_SOULSAND - soulSandCount;
			this.soulSandCount = MAX_SOULSAND;
			System.out.println("total: " + soulSandCount + "\nretour:" + count);
			return count;
		}
			
		soulSandCount += count;
		System.out.println("\ntotal: " + soulSandCount + "\nretour: 0" + "\ntotalsouls: " + soulCount);
		return 0;
	}
	
	public boolean retrieveSoul() {
		if(this.soulCount == 0) return false;
		this.soulCount--;
		return true;
	}

}
