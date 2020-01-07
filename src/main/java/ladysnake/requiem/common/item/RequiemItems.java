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
package ladysnake.requiem.common.item;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

public class RequiemItems {
    public static final FoodComponent HUMAN_FOOD = new FoodComponent.Builder()
            .hunger(6)
            .saturationModifier(0.3F)
            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600, 0), 0.5F)
            .meat()
            .build();

    public static DebugItem DEBUG_ITEM;
    public static Item HUMAN_FLESH;
    public static OpusDemoniumItem OPUS_DEMONIUM;
    public static WrittenOpusItem OPUS_DEMONIUM_CURE;
    public static WrittenOpusItem OPUS_DEMONIUM_CURSE;

    public static void init() {
        DEBUG_ITEM = registerItem(new DebugItem(new Item.Settings()), "debug_item");
        HUMAN_FLESH = registerItem(new Item(new Item.Settings().food(HUMAN_FOOD).group(ItemGroup.FOOD)), "human_flesh");
        OPUS_DEMONIUM = registerItem(new OpusDemoniumItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1)), "opus_daemonium");
        OPUS_DEMONIUM_CURE = registerItem(new WrittenOpusItem(RemnantTypes.MORTAL, Formatting.AQUA, new Item.Settings().group(ItemGroup.MISC).maxCount(1)), "opus_daemonium_cure");
        OPUS_DEMONIUM_CURSE = registerItem(new WrittenOpusItem(RemnantTypes.REMNANT, Formatting.RED, new Item.Settings().group(ItemGroup.MISC).maxCount(1)), "opus_daemonium_curse");
    }

    public static <T extends Item> T registerItem(T item, String name) {
        Registry.register(Registry.ITEM, Requiem.id(name), item);
        return item;
    }
}