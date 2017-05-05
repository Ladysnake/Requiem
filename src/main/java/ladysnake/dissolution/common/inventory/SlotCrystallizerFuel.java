package ladysnake.tartaros.common.inventory;

import ladysnake.tartaros.common.tileentities.TileEntityCrystallizer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCrystallizerFuel extends Slot {

	public SlotCrystallizerFuel(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack)
    {
        return TileEntityCrystallizer.isItemFuel(stack);
    }

}
