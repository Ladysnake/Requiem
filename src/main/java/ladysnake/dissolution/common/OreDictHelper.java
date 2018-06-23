package ladysnake.dissolution.common;

import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;

public class OreDictHelper {

    public static final String PESTLE = "pestle";
    public static final String PESTLE_AND_MORTAR = "pestleAndMortar";
    public static final String HUMAN_FLESH_RAW = "humanFlesh";
    public static final String HUMAN_FLESH_COOKED = "cookedHumanFlesh";

    public static void registerOres() {
        // note: this mod's own items are registered to the ore dictionary using @AutoRegister.Ore
        registerOres(HUMAN_FLESH_RAW, "betterwithmods:mystery_meat");
        registerOres(HUMAN_FLESH_COOKED, "betterwithmods:cooked_mystery_meat");
        OreDictionary.registerOre(HUMAN_FLESH_RAW, ModItems.HUMAN_FLESH_RAW);
        OreDictionary.registerOre(HUMAN_FLESH_COOKED, ModItems.HUMAN_FLESH_COOKED);
//        OreDictionary.registerOre(PESTLE, ModItems.PESTLE);
//        IForgeRegistry<Item> items = RegistryManager.ACTIVE.getRegistry(Item.class);
//        ResourceLocation loc = new ResourceLocation("roots:pestle");
//        if (items.containsKey(loc)) {
//            OreDictionary.registerOre(PESTLE, items.getValue(loc));
//        }
    }

    private static void registerOres(String oreName, String... ids) {
        for (String id : ids) {
            IForgeRegistry<Item> items = ForgeRegistries.ITEMS;
            ResourceLocation loc = new ResourceLocation(id);
            if (items.containsKey(loc)) {
                OreDictionary.registerOre(oreName, items.getValue(loc));
            }
        }
    }

    public static boolean doesItemMatch(ItemStack itemStack, String... ores) {
        return !itemStack.isEmpty() && Arrays.stream(ores).map(OreDictionary::getOreID).anyMatch(id -> Arrays.stream(OreDictionary.getOreIDs(itemStack)).anyMatch(id::equals));
    }
}
