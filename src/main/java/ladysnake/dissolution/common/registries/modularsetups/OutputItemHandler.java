package ladysnake.dissolution.common.registries.modularsetups;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * A IItemHandler implementation that can only output items by default
 *
 * @author Pyrofab
 */
public class OutputItemHandler extends ItemStackHandler {

    /**
     * Replacer for {@link net.minecraftforge.items.IItemHandler#insertItem}, should only be used by the machine proprietary of this handler
     *
     * @param slot     the index of the slots in which to insert the stack
     * @param stack
     * @param simulate
     * @return
     */
    public ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

}
