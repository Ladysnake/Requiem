package ladysnake.tartaros.common.items;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import net.minecraft.item.Item.ToolMaterial;

public class ItemGrandFaux extends ItemScythe {

	public ItemGrandFaux() {
		super(ToolMaterial.DIAMOND);
		this.setUnlocalizedName(Reference.Items.GRAND_FAUX.getUnlocalizedName());
        this.setRegistryName(Reference.Items.GRAND_FAUX.getRegistryName());
        this.setMaxDamage(1500);
	}
}
