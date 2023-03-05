/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.pandemonium.common.item;

import ladysnake.pandemonium.Pandemonium;
import ladysnake.pandemonium.common.remnant.PandemoniumRemnantTypes;
import ladysnake.requiem.common.item.DemonSoulVesselItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Formatting;

public final class PandemoniumItems {
    public static final DemonSoulVesselItem BALEFUL_SOUL_VESSEL = new DemonSoulVesselItem(PandemoniumRemnantTypes.WANDERING_SPIRIT, Formatting.GRAY, new Item.Settings().maxCount(1), "requiem:remnant_vessel.banishment");

    public static void init() {
        registerItem(PandemoniumItems.BALEFUL_SOUL_VESSEL, "baleful_soul_vessel");
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS.getId()).register(entries -> entries.addItem(BALEFUL_SOUL_VESSEL));
    }

    public static <T extends Item> void registerItem(T item, String name) {
        Registry.register(Registries.ITEM, Pandemonium.id(name), item);
    }
}
