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

    public static final DebugItem DEBUG_ITEM = new DebugItem(new Item.Settings());
    public static final Item HUMAN_FLESH = new Item(new Item.Settings().food(HUMAN_FOOD).group(ItemGroup.FOOD));
    public static final OpusDemoniumItem OPUS_DEMONIUM = new OpusDemoniumItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final WrittenOpusItem OPUS_DEMONIUM_CURE = new WrittenOpusItem(RemnantTypes.MORTAL, Formatting.AQUA, new Item.Settings().group(ItemGroup.MISC).maxCount(1));
    public static final WrittenOpusItem OPUS_DEMONIUM_CURSE = new WrittenOpusItem(RemnantTypes.REMNANT, Formatting.RED, new Item.Settings().group(ItemGroup.MISC).maxCount(1));

    public static void init() {
        registerItem(DEBUG_ITEM, "debug_item");
        registerItem(HUMAN_FLESH, "human_flesh");
        registerItem(OPUS_DEMONIUM, "opus_daemonium");
        registerItem(OPUS_DEMONIUM_CURE, "opus_daemonium_cure");
        registerItem(OPUS_DEMONIUM_CURSE, "opus_daemonium_curse");
    }

    public static <T extends Item> void registerItem(T item, String name) {
        Registry.register(Registry.ITEM, Requiem.id(name), item);
    }
}
