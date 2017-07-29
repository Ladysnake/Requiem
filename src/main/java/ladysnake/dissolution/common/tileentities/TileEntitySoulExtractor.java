package ladysnake.dissolution.common.tileentities;

import java.util.Random;

import ladysnake.dissolution.common.blocks.BlockSoulExtractor;
import ladysnake.dissolution.common.config.DissolutionConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntitySoulExtractor extends TileEntity implements ITickable {
	
	public static final int MAX_INPUT = 128;
	private int soulSandCount, soulCount;
	private short processElapsed;
	public boolean keepInventory = false;

	@Override
	public void update() {
		if(this.world.isRemote) return;
		if(this.isEmpty()) {
			this.processElapsed = 0;
			return;
		}
		++processElapsed;
		if(processElapsed%200 == 0) {
			consumeSoulSand();
			Random rand = new Random();
			if(rand.nextInt(30) == 0) soulCount++;
			ItemStack outputStack = new ItemStack(Blocks.SAND);
			if(this.world.getTileEntity(pos.down()) != null && this.world.getTileEntity(pos.down()) instanceof TileEntityHopper){
				TileEntityHopper hopper = ((TileEntityHopper)this.world.getTileEntity(pos.down()));
				IItemHandler handler = hopper.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
				int slot = -1;
				for (int j = 0; j < handler.getSlots() && slot == -1; j ++){
					if (handler.getStackInSlot(j).isEmpty()){
						slot = j;
					}
					else {
						if (handler.getStackInSlot(j).getCount() < handler.getSlotLimit(j) && ItemStack.areItemsEqual(handler.getStackInSlot(j), outputStack)){
							slot = j;
						}
					}
				}
				if (slot != -1)
					handler.insertItem(slot, outputStack, false);
			} else {
				TileEntity tile = getWorld().getTileEntity(pos.up());
				if(tile != null) {
					IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
					if(handler != null) {
						int slot = -1;
						for (int j = 0; j < handler.getSlots() && slot == -1; j ++){
							if (handler.getStackInSlot(j).isEmpty()){
								slot = j;
							}
							else {
								if (handler.getStackInSlot(j).getCount() < handler.getSlotLimit(j) && ItemStack.areItemsEqual(handler.getStackInSlot(j), outputStack) && ItemStack.areItemStackTagsEqual(handler.getStackInSlot(j), outputStack)){
									slot = j;
								}
							}
						}
						if(slot != -1)
							handler.insertItem(slot, outputStack, false);
					}
				} else if (DissolutionConfig.blocks.doSablePop) {
					getWorld().spawnEntity(new EntityItem(getWorld(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, outputStack));
				}
			}
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
		if(soulSandCount + count > MAX_INPUT){
			count -= MAX_INPUT - soulSandCount;
			this.soulSandCount = MAX_INPUT;
			System.out.println("total: " + soulSandCount + "\nretour:" + count);
			return count;
		}
			
		soulSandCount += count;
		markDirty();
	    if (getWorld() != null) {
	    	IBlockState state = getWorld().getBlockState(getPos());
	     	getWorld().notifyBlockUpdate(getPos(), state, state, 3);
	    }
		System.out.println("\ntotal: " + soulSandCount + "\nretour: 0" + "\ntotalsouls: " + soulCount);
		return 0;
	}
	
	protected void consumeSoulSand() {
		soulSandCount--;
		markDirty();
	    if (getWorld() != null) {
	    	BlockSoulExtractor.setState(!this.isEmpty(), this.world, this.pos);
	    }
	}
	
	public boolean retrieveSoul() {
		if(this.soulCount == 0) return false;
		this.soulCount--;
		markDirty();
	    if (getWorld() != null) {
	    	IBlockState state = getWorld().getBlockState(getPos());
	     	getWorld().notifyBlockUpdate(getPos(), state, state, 3);
	    }
		return true;
	}
	
	public void emptyInWorld(World worldIn) {
		if(!worldIn.isRemote)
			worldIn.spawnEntity(new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, new ItemStack(Blocks.SOUL_SAND, soulSandCount)));
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return super.shouldRefresh(world, pos, oldState, newSate) && !keepInventory;
	}

}
