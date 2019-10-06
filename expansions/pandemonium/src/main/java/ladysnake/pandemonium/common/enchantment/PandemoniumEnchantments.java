package ladysnake.pandemonium.common.enchantment;

import ladysnake.pandemonium.Pandemonium;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.registry.Registry;

public final class PandemoniumEnchantments {
    public static final Enchantment ROCK_BODY = new RockBodyEnchantment(Enchantment.Weight.RARE, EnchantmentTarget.ARMOR_CHEST, EquipmentSlot.CHEST);

    public static void init() {
        Registry.register(Registry.ENCHANTMENT, Pandemonium.id("rock_body"), ROCK_BODY);
    }
}
