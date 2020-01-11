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
package ladysnake.requiem.common.loot;

import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.enchantment.InfoEnchantment;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;

import java.util.regex.Pattern;

public final class RequiemLootTables {
    private static final Pattern NETHER_CHEST = Pattern.compile("chests/.*nether.*");
    /** The chance that a nether chest gets a Humanity enchanted book */
    public static final float HUMANITY_CHANCE = 0.7f;
    /** The chance that a humanity book gets level I enchant */
    public static final double BASIC_HUMANITY_CHANCE = 0.9;

    public static void init() {
        LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, identifier, fabricLootSupplierBuilder, lootTableSetter) -> {
            if (NETHER_CHEST.matcher(identifier.getPath()).matches()) {
                fabricLootSupplierBuilder.withPool(FabricLootPoolBuilder.builder()
                    .withRolls(ConstantLootTableRange.create(1))
                    .withEntry(ItemEntry.builder(Items.BOOK).withFunction(() -> (itemStack, lootContext) -> {
                        boolean betterHumanity = lootContext.getRandom().nextFloat() * (1 + lootContext.getLuck()) > BASIC_HUMANITY_CHANCE;
                        InfoEnchantment enchantment = new InfoEnchantment(RequiemEnchantments.HUMANITY, betterHumanity ? 2 : 1);
                        return EnchantedBookItem.forEnchantment(enchantment);
                    }))
                    .withCondition(RandomChanceLootCondition.builder(HUMANITY_CHANCE))
                );
            }
        });
    }
}
