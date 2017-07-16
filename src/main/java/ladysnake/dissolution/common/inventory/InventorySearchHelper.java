package ladysnake.dissolution.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/**
 * Provides various methods for managing inventories
 * 
 * @author Pyrofab
 *
 */
public final class InventorySearchHelper {

	/**
	 * Finds an item in the player inventory
	 * 
	 * @param player
	 *            The player to search
	 * @param item
	 *            The item
	 * @return The first ItemStack in the target inventory corresponding to the
	 *         item
	 */
	public static ItemStack findItem(EntityPlayer player, Item item) {
		if ((player.getHeldItem(EnumHand.OFF_HAND)).getItem() == item) {
			return player.getHeldItem(EnumHand.OFF_HAND);
		} else if ((player.getHeldItem(EnumHand.MAIN_HAND)).getItem() == item) {
			return player.getHeldItem(EnumHand.MAIN_HAND);
		} else {
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (itemstack.getItem() == item) {
					return itemstack;
				}
			}

			return ItemStack.EMPTY;
		}
	}
	
	public static int getSlotFor(InventoryPlayer inv, ItemStack stack)
    {
        for (int i = 0; i < inv.mainInventory.size(); ++i)
        {
            if (!((ItemStack)inv.mainInventory.get(i)).isEmpty() && compareItemStacksExact(stack, inv.mainInventory.get(i)))
            {
                return i;
            }
        }

        return -1;
    }

	/*
	public static ItemStack findItemInstance(EntityPlayer player, Item item) {
		return findItemInstance(player, item.getClass());
	}

	public static ItemStack findItemInstance(EntityPlayer player, Class<? extends Item> item) {
		if (item.isInstance(player.getHeldItem(EnumHand.OFF_HAND).getItem())) {
			return player.getHeldItem(EnumHand.OFF_HAND);
		} else if (item.isInstance(player.getHeldItem(EnumHand.MAIN_HAND))) {
			return player.getHeldItem(EnumHand.MAIN_HAND);
		} else {
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack itemstack = player.inventory.getStackInSlot(i);

				if (item.isInstance(itemstack)) {
					return itemstack;
				}
			}

			return ItemStack.EMPTY;
		}
	}*/
	
	public static boolean compareItemStacksExact(ItemStack stack1, ItemStack stack2)
    {
        return stack1.getItem() == stack2.getItem() && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }

	public static boolean compareItemStacks(ItemStack stack1, ItemStack stack2) {
		return stack2.getItem() == stack1.getItem()
				&& (stack2.getMetadata() == 32767 || stack2.getMetadata() == stack1.getMetadata());
	}
	
	private InventorySearchHelper(){}
	
}
