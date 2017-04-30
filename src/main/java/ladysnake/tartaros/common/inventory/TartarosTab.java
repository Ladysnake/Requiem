package ladysnake.tartaros.common.inventory;

import ladysnake.tartaros.common.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class TartarosTab extends CreativeTabs {

	public TartarosTab() {
		super("tartaros");
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(ModItems.ECTOPLASM);
	}

}
