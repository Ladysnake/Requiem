package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.Dissolution;
import net.minecraft.item.Item.ToolMaterial;

public class ItemGrandFaux extends ItemScythe {

	public ItemGrandFaux() {
		super(ToolMaterial.DIAMOND);
		this.setUnlocalizedName(Reference.Items.ANCIENT_SCYTHE.getUnlocalizedName());
        this.setRegistryName(Reference.Items.ANCIENT_SCYTHE.getRegistryName());
        this.setMaxDamage(1500);
	}
}
