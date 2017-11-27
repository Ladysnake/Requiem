package ladysnake.dissolution.common.items;

import net.minecraft.item.ItemStack;

public class ItemDepletedCoal extends ItemDepleted {

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        return 1200;
    }
}
