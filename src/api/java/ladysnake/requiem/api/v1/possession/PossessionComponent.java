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
package ladysnake.requiem.api.v1.possession;

import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import net.minecraft.entity.mob.MobEntity;

import javax.annotation.CheckForNull;

/**
 * A {@link PossessionComponent} handles a player's possession status.
 */
public interface PossessionComponent extends SyncedComponent {
    boolean startPossessing(MobEntity mob);

    boolean canStartPossessing(MobEntity mob);

    void stopPossessing();

    void stopPossessing(boolean transfer);

    @CheckForNull
    MobEntity getPossessedEntity();

    boolean isPossessing();
}
