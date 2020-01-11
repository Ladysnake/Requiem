package ladysnake.requiem.common.enchantment;

import ladysnake.requiem.Requiem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RequiemEnchantments {
    public static final Identifier HUMANITY_ID = Requiem.id("humanity");
    public static final Enchantment HUMANITY = new HumanityEnchantment(
        Enchantment.Weight.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}
    );

    public static void init() {
        Registry.register(Registry.ENCHANTMENT, HUMANITY_ID, HUMANITY);
    }
}
