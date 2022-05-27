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
package ladysnake.requiem.common.tag;

import ladysnake.requiem.Requiem;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public final class RequiemEntityTypeTags {
    public static final TagKey<EntityType<?>> DICHROMATS = register("vision/dichromats");
    public static final TagKey<EntityType<?>> HEMERALOPES = register("vision/hemeralopes");
    public static final TagKey<EntityType<?>> NIGHT_EYED = register("vision/night_eyed");
    public static final TagKey<EntityType<?>> TETRACHROMATS = register("vision/tetrachromats");

    public static final TagKey<EntityType<?>> POSSESSABLES = register("possession/possessables");

    public static final TagKey<EntityType<?>> ARMOR_USERS = register("inventory/armor_users");
    public static final TagKey<EntityType<?>> HOTBAR_CARRIERS = register("inventory/hotbar_carriers");
    public static final TagKey<EntityType<?>> SUPERCRAFTERS = register("inventory/supercrafters");

    public static final TagKey<EntityType<?>> ARROW_GENERATORS = register("behavior/arrow_generators");

    public static final TagKey<EntityType<?>> REPLACEABLE_SKELETONS = register("transformation/replaceable_skeletons");

    public static final TagKey<EntityType<?>> SKELETONS = register("skeletons");
    public static final TagKey<EntityType<?>> ZOMBIES = register("zombies");
    public static final TagKey<EntityType<?>> PIGLINS = register("piglins");

    public static void init() {
        // NO-OP
    }

    public static TagKey<EntityType<?>> register(String name) {
        return TagKey.of(Registry.ENTITY_TYPE_KEY, Requiem.id(name));
    }
}
