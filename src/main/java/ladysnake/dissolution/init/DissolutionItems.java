package ladysnake.dissolution.init;

import ladysnake.dissolution.item.DebugItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

import static ladysnake.dissolution.Dissolution.MODID;

public class DissolutionItems {
    public static Item DEBUG_ITEM;

    public static void init() {
        DEBUG_ITEM = registerItem(new DebugItem(new Item.Settings()), "debug_item");
    }

    public static Item registerItem(Item item, String name) {
        Registry.register(Registry.ITEM, MODID + ":" + name, item);
        return item;
    }
}