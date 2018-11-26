package ladysnake.dissolution.common;

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

    public static void registerOres() {
        // note: this mod's own items are registered to the ore dictionary using @AutoRegister.Ore
        registerOres(HUMAN_FLESH_RAW,
                "betterwithmods:mystery_meat",
                "evilcraft:werewolf_flesh",
                "cannibalism:playerflesh",
                "cannibalism:villagerflesh",
                "cannibalism:witchflesh");
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
