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
package ladysnake.requiem.common;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.common.entity.HorologistManager;
import ladysnake.requiem.common.impl.remnant.RevivingDeathSuspender;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.event.LevelComponentCallback;
import net.minecraft.entity.player.PlayerEntity;

public final class RequiemComponents {
    public static final ComponentType<DeathSuspender> DEATH_SUSPENDER = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("death_suspension"), DeathSuspender.class
    ).attach(EntityComponentCallback.event(PlayerEntity.class), RevivingDeathSuspender::new);

    public static final ComponentType<HorologistManager> HOROLOGIST_MANAGER = ComponentRegistry.INSTANCE.registerIfAbsent(
        Requiem.id("horologist_manager"), HorologistManager.class
    ).attach(LevelComponentCallback.EVENT, p -> new HorologistManager());

    public static void init() {
        // NO-OP
    }
}
