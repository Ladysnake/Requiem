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
package ladysnake.requiem.core.tag;

import ladysnake.requiem.core.RequiemCore;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public final class RequiemCoreTags {
    public static final class Entity {
        public static final TagKey<EntityType<?>> FRICTIONLESS_HOSTS = register("possession/frictionless_hosts");
        public static final TagKey<EntityType<?>> POSSESSION_BLACKLIST = register("possession/possession_blacklist");
        public static final TagKey<EntityType<?>> SOUL_CAPTURE_BLACKLIST = register("possession/soul_capture_blacklist");
        public static final TagKey<EntityType<?>> GOLEMS = register("golems");
        public static final TagKey<EntityType<?>> ITEM_USERS = register("inventory/item_users");
        public static final TagKey<EntityType<?>> INVENTORY_CARRIERS = register("inventory/inventory_carriers");
        public static final TagKey<EntityType<?>> EATERS = register("behavior/regular_eaters");
        public static final TagKey<EntityType<?>> SLEEPERS = register("behavior/regular_sleepers");
        public static final TagKey<EntityType<?>> IMMOVABLE = register("behavior/immovable");
        public static final TagKey<EntityType<?>> ARMOR_BANNED = register("inventory/armor_banned");
        public static final TagKey<EntityType<?>> SOULLESS = register("possession/soulless");

        private static TagKey<EntityType<?>> register(String name) {
            return TagKey.of(Registry.ENTITY_TYPE_KEY, RequiemCore.id(name));
        }
    }

    public static final class Item {
        public static final TagKey<net.minecraft.item.Item> UNDEAD_CURES = TagKey.of(Registry.ITEM_KEY, RequiemCore.id("undead_cures"));
    }
}
