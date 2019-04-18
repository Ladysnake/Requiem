package ladysnake.requiem.common.item;

import ladysnake.requiem.Requiem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class RequiemItems {
    public static Item DEBUG_ITEM;

    public static void init() {
        DEBUG_ITEM = registerItem(new DebugItem(new Item.Settings()), "debug_item");
    }

    public static Item registerItem(Item item, String name) {
        Registry.register(Registry.ITEM, Requiem.id(name), item);
        return item;
    }
}