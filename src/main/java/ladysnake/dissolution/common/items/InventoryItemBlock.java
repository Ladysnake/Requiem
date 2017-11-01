package ladysnake.dissolution.common.items;

import ladysnake.dissolution.api.IGenericInventoryItem;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class InventoryItemBlock extends ItemBlock implements IGenericInventoryItem {
    public InventoryItemBlock(Block block) {
        super(block);
    }
}
