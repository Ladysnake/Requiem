package ladysnake.dissolution.common;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;

public class OreDictHelper {

    public static final String PESTLE = "pestle";
    public static final String PESTLE_AND_MORTAR = "pestleAndMortar";

    public static void registerOres() {
//        OreDictionary.registerOre(PESTLE, ModItems.PESTLE);
//        IForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(Item.class);
//        ResourceLocation loc = new ResourceLocation("roots:pestle");
//        if (items.containsKey(loc)) {
//            OreDictionary.registerOre(PESTLE, items.getValue(loc));
//        }
    }

    public static boolean doesItemMatch(ItemStack itemStack, String... ores) {
        return !itemStack.isEmpty() && Arrays.stream(ores).map(OreDictionary::getOreID).anyMatch(id -> Arrays.stream(OreDictionary.getOreIDs(itemStack)).anyMatch(id::equals));
    }
}
