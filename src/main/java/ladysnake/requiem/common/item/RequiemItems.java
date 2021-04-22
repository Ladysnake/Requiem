/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.common.item;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public class RequiemItems {
    public static final FoodComponent HUMAN_FOOD = new FoodComponent.Builder()
            .hunger(6)
            .saturationModifier(0.3F)
            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.5F)
            .meat()
            .build();

    public static final DebugItem DEBUG_ITEM = new DebugItem(new Item.Settings());
    public static final Item TOTEM_OF_SKELETONIZATION = new Item((new Item.Settings()).maxCount(1).group(ItemGroup.COMBAT).rarity(Rarity.UNCOMMON));
    public static final OpusDemoniumItem OPUS_DEMONIUM = new OpusDemoniumItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final WrittenOpusItem OPUS_DEMONIUM_CURE = new WrittenOpusItem(RemnantTypes.MORTAL, Formatting.AQUA, new Item.Settings().group(ItemGroup.MISC).maxCount(1), "requiem:opus_daemonium.cure");
    public static final WrittenOpusItem OPUS_DEMONIUM_CURSE = new WrittenOpusItem(RemnantTypes.REMNANT, Formatting.RED, new Item.Settings().group(ItemGroup.MISC).maxCount(1), "requiem:opus_daemonium.curse");
    public static final WrittenOpusItem OPUS_DEMONIUM_BANISHMENT = new WrittenOpusItem(RemnantTypes.WANDERING_SPIRIT, Formatting.GRAY, new Item.Settings().group(ItemGroup.MISC).maxCount(1), "requiem:opus_daemonium.banishment");

    public static void init() {
        registerItem(DEBUG_ITEM, "debug_item");
        registerItem(TOTEM_OF_SKELETONIZATION, "totem_of_skeletonization");
        registerItem(OPUS_DEMONIUM, "opus_daemonium");
        registerItem(OPUS_DEMONIUM_CURE, "opus_daemonium_cure");
        registerItem(OPUS_DEMONIUM_CURSE, "opus_daemonium_curse");
        registerItem(OPUS_DEMONIUM_BANISHMENT, "opus_daemonium_banishment");
    }

    public static <T extends Item> void registerItem(T item, String name) {
        Registry.register(Registry.ITEM, Requiem.id(name), item);
    }
}
