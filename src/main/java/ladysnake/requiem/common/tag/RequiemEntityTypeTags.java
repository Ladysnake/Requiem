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
import net.minecraft.entity.EntityType;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.Tag;

public final class RequiemEntityTypeTags {
    public static final Tag<EntityType<?>> POSSESSION_BLACKLIST = register("possession_blacklist");
    public static final Tag<EntityType<?>> ITEM_USER = register("item_user");
    public static final Tag<EntityType<?>> IMMOVABLE = register("immovable");
    public static final Tag<EntityType<?>> EATER = register("regular_eater");

    public static Tag<EntityType<?>> register(String name) {
        return new EntityTypeTags.CachingTag(Requiem.id(name));
    }

}
