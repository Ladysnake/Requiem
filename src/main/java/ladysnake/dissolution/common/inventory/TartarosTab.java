package ladysnake.dissolution.common.inventory;

import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class TartarosTab extends CreativeTabs {

	public TartarosTab() {
		super("tartaros");
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(ModItems.BASE_RESOURCE, 1, 0);
	}

}
