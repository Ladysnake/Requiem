package ladysnake.requiem.common.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class HumanityEnchantment extends Enchantment {
    protected HumanityEnchantment(Weight weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
        super(weight, type, slotTypes);
    }

    public int getMinimumPower(int level) {
        return level * 10;
    }

    public int getMaximumPower(int level) {
        return this.getMinimumPower(level) + 15;
    }

    public boolean isTreasure() {
        return true;
    }

    public int getMaximumLevel() {
        return 2;
    }

}
