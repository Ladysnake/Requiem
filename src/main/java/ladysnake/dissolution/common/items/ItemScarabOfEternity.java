package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import net.minecraft.item.Item;

public class ItemLifeProtectionRing extends Item {

	public ItemLifeProtectionRing() {
		super();
		setUnlocalizedName(Reference.Items.LIFE_PROTECTION_RING.getUnlocalizedName());
        setRegistryName(Reference.Items.LIFE_PROTECTION_RING.getRegistryName());
        this.setCreativeTab(Dissolution.CREATIVE_TAB);
	}
	
}
