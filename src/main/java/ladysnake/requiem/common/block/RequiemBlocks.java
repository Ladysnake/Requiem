package ladysnake.requiem.common.block;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.item.RequiemItems;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public class RequiemBlocks {

    public static void init() {

    }

    private static Block registerBlock(Block block, String name) {
        return registerBlock(block, name, true);
    }

    private static Block registerBlock(Block block, String name, boolean doItem) {
        Registry.register(Registry.BLOCK, Requiem.id(name), block);

        if (doItem) {
            BlockItem item = new BlockItem(block, new Item.Settings().itemGroup(ItemGroup.DECORATIONS));
            item.registerBlockItemMap(Item.BLOCK_ITEM_MAP, item);
            RequiemItems.registerItem(item, name);
        }

        return block;
    }

}
