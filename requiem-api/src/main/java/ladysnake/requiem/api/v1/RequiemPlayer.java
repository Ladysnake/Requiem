/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.player.PlayerEntity;

/**
 * A player with extended capabilities allowing interaction with specific Requiem
 * functionality. When an API provider is installed, every {@link PlayerEntity}
 * implements this interface.
 *
 * @since 1.0.0
 */
public interface RequiemPlayer {

    // exists because Immersive Portals still uses it
    default PossessionComponent asPossessor() {
        return PossessionComponent.KEY.get(this);
    }

}
