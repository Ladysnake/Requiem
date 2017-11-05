package ladysnake.dissolution.common.inventory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class InputItemHandler extends ItemStackHandler {
	
	private final Set<Item> whiteList;
	private int maxSize = 64;
	
	public InputItemHandler(Item... whitelist) {
		Collections.addAll(this.whiteList = new HashSet<>(), whitelist);
	}
	
	public InputItemHandler(Block... whitelist) {
		this.whiteList = new HashSet<>();
		for(Block b : whitelist)
			this.whiteList.add(Item.getItemFromBlock(b));
	}
	
	public void addWhitelistedItem(Item item) {
		this.whiteList.add(item);
	}

	public ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
		return super.insertItem(slot, stack, simulate);
	}
	
	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if(whiteList.isEmpty() || whiteList.contains(stack.getItem()))
			stack = super.insertItem(slot, stack, simulate);
		return stack;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public int getSlotLimit(int slot) {
		return maxSize;
	}

	public boolean isWhitelisted(ItemStack item) {
		return this.whiteList.isEmpty() || this.whiteList.contains(item.getItem());
	}
}
