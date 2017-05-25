package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.Dissolution;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemSoulGem extends Item {

	public ItemSoulGem() {
		super();
		this.setUnlocalizedName(Reference.Items.SOULGEM.getUnlocalizedName());
        this.setRegistryName(Reference.Items.SOULGEM.getRegistryName());
        this.setCreativeTab(Dissolution.CREATIVE_TAB);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}
	
}
