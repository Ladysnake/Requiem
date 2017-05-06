package ladysnake.dissolution.common.items;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.Tartaros;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemBaseResource extends Item {

	public static final int TOTAL_VARIANTS = 5;
	public static final List<String> names = new ArrayList<String>();

	static {
		names.add("ectoplasm");
		names.add("ectoplasma");
		names.add("soulfur");
		names.add("mercury");
		names.add("cinnabar");
	}

	public ItemBaseResource() {
		super();
		setUnlocalizedName(Reference.Items.BASE_RESOURCE.getUnlocalizedName());
		setRegistryName(Reference.Items.BASE_RESOURCE.getRegistryName());
		this.setCreativeTab(Tartaros.CREATIVE_TAB);
		this.setHasSubtypes(true);
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < TOTAL_VARIANTS; ++i) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.getMetadata() < names.size())	
			return "item." + names.get(stack.getMetadata());
		return super.getUnlocalizedName(stack);
	}

}
