package ladysnake.requiem.common.enchantment;

import ladysnake.requiem.Requiem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.registry.Registry;

public class RequiemEnchantments {
    public static final Enchantment HUMANITY = new HumanityEnchantment(
        Enchantment.Weight.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}
    );

    public static void init() {
        registerEnchantment(HUMANITY, "humanity");
    }

    public static void registerEnchantment(Enchantment enchant, String name) {
        Registry.register(Registry.ENCHANTMENT, Requiem.id(name), enchant);
    }
}
