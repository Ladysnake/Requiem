package ladysnake.requiem.mixin.item;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantedBookItem.class)
public abstract class EnchantedBookItemMixin extends Item {
    public EnchantedBookItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addHumanityModelOverride(Item.Settings settings, CallbackInfo ci) {
        this.addPropertyGetter(Requiem.id("humanity"), (stack, world, entity) -> {
            ListTag enchantments = EnchantedBookItem.getEnchantmentTag(stack);
            for (int i = 0; i < enchantments.size(); i++) {
                CompoundTag tag = enchantments.getCompound(i);
                Identifier enchantId = Identifier.tryParse(tag.getString("id"));
                if (enchantId != null && enchantId.equals(RequiemEnchantments.HUMANITY_ID)) {
                    return tag.getInt("lvl");
                }
            }
            return 0F;
        });
    }
}
