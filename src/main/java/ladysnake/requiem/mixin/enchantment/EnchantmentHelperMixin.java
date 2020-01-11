package ladysnake.requiem.mixin.enchantment;

import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.InfoEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "getHighestApplicableEnchantmentsAtPower", at = @At(value = "RETURN"))
    private static void skipRandomHumanityBookEnchant(int power, ItemStack stack, boolean bl, CallbackInfoReturnable<List<InfoEnchantment>> cir) {
        if (stack.getItem() == Items.BOOK) {
            cir.getReturnValue().removeIf(enchantment -> enchantment.enchantment == RequiemEnchantments.HUMANITY);
        }
    }
}
