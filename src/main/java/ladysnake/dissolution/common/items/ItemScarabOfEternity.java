package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import net.minecraft.item.Item;

public class ItemScarabOfEternity extends Item {

	public ItemScarabOfEternity() {
		super();
		setUnlocalizedName(Reference.Items.SCARAB_OF_ETERNITY.getUnlocalizedName());
        setRegistryName(Reference.Items.SCARAB_OF_ETERNITY.getRegistryName());
        this.setCreativeTab(Dissolution.CREATIVE_TAB);
        this.setMaxStackSize(1);
	}
	
}
