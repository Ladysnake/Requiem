package ladysnake.dissolution.common.block;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.common.item.DissolutionItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.block.BlockItem;
import net.minecraft.util.registry.Registry;

public class DissolutionBlocks {

    public static void init() {

    }

    private static Block registerBlock(Block block, String name) {
        return registerBlock(block, name, true);
    }

    private static Block registerBlock(Block block, String name, boolean doItem) {
        Registry.register(Registry.BLOCK, Dissolution.id(name), block);

        if (doItem) {
            BlockItem item = new BlockItem(block, new Item.Settings().itemGroup(ItemGroup.DECORATIONS));
            item.registerBlockItemMap(Item.BLOCK_ITEM_MAP, item);
            DissolutionItems.registerItem(item, name);
        }

        return block;
    }

}
