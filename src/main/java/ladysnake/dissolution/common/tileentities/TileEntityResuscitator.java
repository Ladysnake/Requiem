package ladysnake.dissolution.common.tileentities;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityResuscitator extends TileEntity {

	private int ItemCount;
	public ArrayList<ItemStack> itemName = new ArrayList<ItemStack>();

	public TileEntityResuscitator() {

		ItemCount = 0;

	}



	public void AddItem(ItemStack item) {
		itemName.add(item);
	}

	public void RemoveItem(ItemStack item) {
		itemName.removeAll(itemName);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.ItemCount = compound.getInteger("itemcount");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("itemcount", ItemCount);
		return compound;
	}

}
