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
package ladysnake.requiem.mixin.server;

public final class PlayerTagKeys {
    private PlayerTagKeys() { throw new AssertionError(); }

    public static final String POSSESSED_ROOT_TAG = "requiem:possessed_root";
    public static final String POSSESSED_ENTITY_TAG = "requiem:possessed_entity";
    public static final String POSSESSED_UUID_TAG = "requiem:possessed_uuid";
}
