/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
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
