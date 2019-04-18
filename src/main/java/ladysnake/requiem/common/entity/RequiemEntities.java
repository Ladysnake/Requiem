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
package ladysnake.requiem.common.entity;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

public class RequiemEntities {
    public static EntityType<PlayerShellEntity> PLAYER_SHELL;

    public static void init() {
        PLAYER_SHELL = Registry.register(
                Registry.ENTITY_TYPE,
                Requiem.id("player_shell"),
                FabricEntityTypeBuilder.create(EntityCategory.MISC, PlayerShellEntity::new)
                        .size(EntitySize.resizeable(EntityType.PLAYER.getWidth(), EntityType.PLAYER.getHeight()))
                        .trackable(64, 1, true)
                        .build()
        );
    }
}
