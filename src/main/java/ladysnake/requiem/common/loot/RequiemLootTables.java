/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.loot;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import ladysnake.requiem.mixin.common.access.LootContextTypesAccessor;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.registry.Registry;

import java.util.regex.Pattern;

public final class RequiemLootTables {
    public static final LootContextType POSSESSION = LootContextTypesAccessor.requiem$register(
        "requiem:possession",
        builder -> builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN)
    );
    public static final LootConditionType RIFT_MORTICIAN_CONDITION = new LootConditionType(new RiftMorticianLootCondition.Serializer());

    private static final Pattern NETHER_CHEST = Pattern.compile("chests/.*nether.*");
    /** The chance that a nether chest gets a Humanity enchanted book */
    public static final float HUMANITY_CHANCE = 0.7f;
    /** The chance that a humanity book gets level I enchant */
    public static final double BASIC_HUMANITY_CHANCE = 0.9;

    public static void init() {
        Registry.register(Registry.LOOT_CONDITION_TYPE, Requiem.id("rift_mortician"), RIFT_MORTICIAN_CONDITION);

        LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, identifier, fabricLootSupplierBuilder, lootTableSetter) -> {
            if (NETHER_CHEST.matcher(identifier.getPath()).matches()) {
                fabricLootSupplierBuilder.withPool(FabricLootPoolBuilder.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .withEntry(ItemEntry.builder(Items.BOOK).apply(() -> new LootFunction() {
                        @Override
                        public LootFunctionType getType() {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
                            boolean betterHumanity = lootContext.getRandom().nextFloat() * (1 + lootContext.getLuck()) > BASIC_HUMANITY_CHANCE;
                            EnchantmentLevelEntry enchantment = new EnchantmentLevelEntry(RequiemEnchantments.HUMANITY, betterHumanity ? 2 : 1);
                            return EnchantedBookItem.forEnchantment(enchantment);
                        }
                    }).build())
                    .withCondition(RandomChanceLootCondition.builder(HUMANITY_CHANCE).build())
                    .build()
                );
            }
        });
    }
}
