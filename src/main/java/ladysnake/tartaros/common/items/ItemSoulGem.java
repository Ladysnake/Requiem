package ladysnake.tartaros.common.items;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemSoulGem extends Item {

	public ItemSoulGem() {
		super();
		this.setUnlocalizedName(Reference.Items.SOULGEM.getUnlocalizedName());
        this.setRegistryName(Reference.Items.SOULGEM.getRegistryName());
        this.setCreativeTab(Tartaros.CREATIVE_TAB);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}
	
}
