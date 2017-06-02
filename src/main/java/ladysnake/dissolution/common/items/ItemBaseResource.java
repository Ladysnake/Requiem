package ladysnake.dissolution.common.items;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemBaseResource extends Item {

	public static final List<String> variants = new ArrayList<String>();

	static {
		variants.add("ectoplasm");
		variants.add("ectoplasma");
		variants.add("sulfur");
		variants.add("cinnabar");
		variants.add("mercury");
	}

	public ItemBaseResource() {
		super();
		setUnlocalizedName(Reference.Items.BASE_RESOURCE.getUnlocalizedName());
		setRegistryName(Reference.Items.BASE_RESOURCE.getRegistryName());
		this.setCreativeTab(Dissolution.CREATIVE_TAB);
		this.setHasSubtypes(true);
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < variants.size(); ++i) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.getMetadata() < variants.size())
			return "item." + variants.get(stack.getMetadata());
		return super.getUnlocalizedName(stack);
	}

	public static ItemStack resourceFromName(String name) {
		return resourceFromName(name, 1);
	}
	
	public static ItemStack resourceFromName(String name, int quantity) {
		if (variants.contains(name))
			return new ItemStack(ModItems.BASE_RESOURCE, quantity, variants.indexOf(name));
		return null;
	}

}
