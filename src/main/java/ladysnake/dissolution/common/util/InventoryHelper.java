package ladysnake.dissolution.common.util;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class InventoryHelper {
    public static void transferEquipment(LivingEntity source, LivingEntity dest) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stuff = source.getEquippedStack(slot);
            if (stuff.isEmpty()) {
                continue;
            }
            if (!dest.getEquippedStack(slot).isEmpty()) {
                dest.dropStack(stuff, 0.5f);
            } else {
                dest.setEquippedStack(slot, stuff);
            }
            source.setEquippedStack(slot, ItemStack.EMPTY);
        }
    }
}
