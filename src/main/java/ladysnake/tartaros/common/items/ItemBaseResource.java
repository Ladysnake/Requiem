package ladysnake.tartaros.common.items;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemBaseResource extends Item {

	public ItemBaseResource() {
		super();
		setUnlocalizedName(Reference.Items.BASE_RESOURCE.getUnlocalizedName());
        setRegistryName(Reference.Items.BASE_RESOURCE.getRegistryName());
        this.setCreativeTab(Tartaros.CREATIVE_TAB);
        this.setHasSubtypes(true);
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < 16; ++i)
        {
            subItems.add(new ItemStack(itemIn, 1, i));
        }
	}
	
}
