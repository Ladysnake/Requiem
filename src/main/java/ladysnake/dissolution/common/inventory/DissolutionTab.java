package ladysnake.dissolution.common.inventory;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class DissolutionTab extends CreativeTabs {

	public DissolutionTab() {
		super(Reference.MOD_ID);
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(ModItems.ANCIENT_SCYTHE);
	}

}
