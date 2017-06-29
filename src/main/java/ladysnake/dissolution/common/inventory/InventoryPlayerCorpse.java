package ladysnake.dissolution.common.inventory;

import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class InventoryPlayerCorpse implements IInventory {
	
	/** An array of 36 item stacks indicating the main player inventory (including the visible bar). */
    public NonNullList<ItemStack> mainInventory = NonNullList.<ItemStack>withSize(36, ItemStack.EMPTY);
    private EntityPlayerCorpse corpseEntity;
    private boolean dirty;
    
    public InventoryPlayerCorpse(EntityPlayerCorpse corpse) {
    	this(NonNullList.withSize(36, ItemStack.EMPTY), corpse);
	}
    
	public InventoryPlayerCorpse(NonNullList<ItemStack> inv, EntityPlayerCorpse corpse) {
		super();
		for(int i = 0; i < inv.size(); i++)
			this.mainInventory.set(i, inv.get(i));
		this.corpseEntity = corpse;
	}

	@Override
	public String getName() {
		return this.corpseEntity.getName() + "'s stuff";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentTranslation(this.getName());
	}

	@Override
	public int getSizeInventory() {
		return this.mainInventory.size();
	}

	@Override
	public boolean isEmpty() {
		return !this.mainInventory.stream().anyMatch(e -> !e.isEmpty());
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return this.mainInventory.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return !(this.mainInventory.get(index)).isEmpty() ? ItemStackHelper.getAndSplit(this.mainInventory, index, count) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		try {
			ItemStack ret = this.mainInventory.get(index);
			this.mainInventory.set(index, ItemStack.EMPTY);
			return ret;
		} catch (ArrayIndexOutOfBoundsException e) {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		this.mainInventory.set(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {
		this.dirty = true;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.corpseEntity.getDistanceSqToEntity(player) < 50;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		this.mainInventory.clear();
	}
	
	public void dropAllItems(Entity entityIn) {
		mainInventory.forEach(stack -> entityIn.entityDropItem(stack, 0.5f));
	}
	
	public NBTTagList writeToNBT(NBTTagList nbtTagListIn) {
		for (int i = 0; i < this.mainInventory.size(); ++i)
        {
            if (!((ItemStack)this.mainInventory.get(i)).isEmpty())
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                ((ItemStack)this.mainInventory.get(i)).writeToNBT(nbttagcompound);
                nbtTagListIn.appendTag(nbttagcompound);
            }
        }
		return nbtTagListIn;
	}
	
	public void readFromNBT(NBTTagList nbtTagListIn)
    {
        this.mainInventory.clear();

        for (int i = 0; i < nbtTagListIn.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = nbtTagListIn.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = new ItemStack(nbttagcompound);

            if (!itemstack.isEmpty())
            {
                if (j >= 0 && j < this.mainInventory.size())
                {
                    this.mainInventory.set(j, itemstack);
                }
            }
        }
    }

	@Override
	public String toString() {
		return "InventoryPlayerCorpse [mainInventory=" + mainInventory + ", dirty="
				+ dirty + "]";
	}
	
}
