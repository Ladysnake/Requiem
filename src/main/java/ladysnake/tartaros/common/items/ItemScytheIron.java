package ladysnake.tartaros.common.items;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.Tartaros;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;

public class ItemScytheIron extends ItemSword {

	public ItemScytheIron() {
		super(ToolMaterial.IRON);
		this.setUnlocalizedName(Reference.Items.SCYTHE_IRON.getUnlocalizedName());
        this.setRegistryName(Reference.Items.SCYTHE_IRON.getRegistryName());
        this.setCreativeTab(Tartaros.CREATIVE_TAB);
        this.setMaxStackSize(1);
        this.setMaxDamage(50);
	}
}
