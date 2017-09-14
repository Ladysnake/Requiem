package ladysnake.dissolution.common.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemOccularePart extends Item {
	
	private final int id;
	
	public ItemOccularePart(int id) {
		this(id, 0);
	}
	
	public ItemOccularePart(int id, int durability) {
		super();
		this.id = id;
		this.setMaxDamage(durability);
	}

	public int getId() {
		return id;
	}
	
}
