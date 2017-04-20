package ladysnake.tartaros.common.items;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import net.minecraft.item.Item;

public class ItemSoulInABottle extends Item {

	public ItemSoulInABottle() {
		super();
		setUnlocalizedName(Reference.Items.SOULINABOTTLE.getUnlocalizedName());
        setRegistryName(Reference.Items.SOULINABOTTLE.getRegistryName());
        this.setCreativeTab(Tartaros.CREATIVE_TAB);
	}
}
