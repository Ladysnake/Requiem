package ladysnake.dissolution.common.inventory;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.items.ItemBaseResource;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class DissolutionTab extends CreativeTabs {

	public DissolutionTab() {
		super(Reference.MOD_ID);
	}
	
	@Override
	public ItemStack getTabIconItem() {
		return ItemBaseResource.resourceFromName("ectoplasm");
	}

}
