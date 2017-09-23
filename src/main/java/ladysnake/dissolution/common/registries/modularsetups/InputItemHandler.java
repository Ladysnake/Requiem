package ladysnake.dissolution.common.registries.modularsetups;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class InputItemHandler extends ItemStackHandler {
	
	private Set<Item> whiteList;
	
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
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if(whiteList.isEmpty() || whiteList.contains(stack.getItem()))
			stack = super.insertItem(slot, stack, simulate);
		return stack;
	}

}
