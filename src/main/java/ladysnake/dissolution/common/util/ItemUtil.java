package ladysnake.dissolution.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

public final class ItemUtil {
    private ItemUtil() { throw new AssertionError();}

    public static boolean isWaterBottle(ItemStack item) {
        return item.getItem() == Items.POTION && PotionUtil.getPotion(item) == Potions.WATER;
    }
}
