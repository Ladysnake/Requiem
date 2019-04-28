/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
package ladysnake.requiem.common.tag;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;

public class RequiemItemTags {
    public static final Tag<Item> BONES = TagRegistry.item(Requiem.id("bones"));
    public static final Tag<Item> UNDEAD_CURES = TagRegistry.item(Requiem.id("undead_cures"));
    public static final Tag<Item> RAW_MEATS = TagRegistry.item(Requiem.id("raw_meats"));
}
