package ladysnake.requiem.mixin.loot.function;

import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(EnchantRandomlyLootFunction.class)
public abstract class EnchantRandomlyLootFunctionMixin {
    @ModifyVariable(method = "process", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1))
    private List<Enchantment> skipRandomHumanityEnchant(List<Enchantment> enchantPool) {
        enchantPool.removeIf(RequiemEnchantments.HUMANITY::equals);
        return enchantPool;
    }
}
