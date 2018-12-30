package ladysnake.dissolution.common.item;

import ladysnake.dissolution.Dissolution;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class DissolutionItems {
    public static Item DEBUG_ITEM;

    public static void init() {
        DEBUG_ITEM = registerItem(new DebugItem(new Item.Settings()), "debug_item");
    }

    public static Item registerItem(Item item, String name) {
        Registry.register(Registry.ITEM, Dissolution.id(name), item);
        return item;
    }
}