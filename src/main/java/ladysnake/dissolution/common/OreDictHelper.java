package ladysnake.dissolution.common;

import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.Arrays;

public class OreDictHelper {

    public static final String PESTLE = "pestle";
    public static final String PESTLE_AND_MORTAR = "pestleAndMortar";

    public static void registerOres() {
        OreDictionary.registerOre(PESTLE, ModItems.PESTLE);
        IForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(Item.class);
        ResourceLocation loc;
        if (items.containsKey(loc = new ResourceLocation("roots:pestle")))
            OreDictionary.registerOre(PESTLE, items.getValue(loc));
    }

    public static boolean doesItemMatch(ItemStack itemStack, String... ores) {
        return !itemStack.isEmpty() && Arrays.stream(ores).map(OreDictionary::getOreID).anyMatch(id -> Arrays.stream(OreDictionary.getOreIDs(itemStack)).anyMatch(id::equals));
    }
}
