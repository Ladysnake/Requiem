package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.Tartaros;
import net.minecraft.item.Item;

public class ItemSoulInABottle extends Item {

	public ItemSoulInABottle() {
		super();
		setUnlocalizedName(Reference.Items.SOULINABOTTLE.getUnlocalizedName());
        setRegistryName(Reference.Items.SOULINABOTTLE.getRegistryName());
        this.setCreativeTab(Tartaros.CREATIVE_TAB);
	}
}
