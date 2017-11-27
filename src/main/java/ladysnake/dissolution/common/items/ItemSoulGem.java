package ladysnake.dissolution.common.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemSoulGem extends Item {

    public ItemSoulGem() {
        super();
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

}
