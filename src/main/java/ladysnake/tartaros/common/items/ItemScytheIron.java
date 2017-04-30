package ladysnake.tartaros.common.items;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import net.minecraft.item.Item.ToolMaterial;

public class ItemScytheIron extends ItemScythe {

	public ItemScytheIron() {
		super(ToolMaterial.IRON);
		this.setUnlocalizedName(Reference.Items.SCYTHE_IRON.getUnlocalizedName());
        this.setRegistryName(Reference.Items.SCYTHE_IRON.getRegistryName());
        this.setMaxDamage(255);
	}
}
