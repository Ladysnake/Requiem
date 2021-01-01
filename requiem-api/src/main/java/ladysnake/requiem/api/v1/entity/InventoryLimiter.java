/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.api.v1.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.util.Identifier;

public interface InventoryLimiter extends Component {
    ComponentKey<InventoryLimiter> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "inventory_limiter"), InventoryLimiter.class);

    void setEnabled(boolean enabled);
    boolean isEnabled();
    void unlock(InventoryPart part);
    void lock(InventoryPart part);
    boolean isLocked(InventoryPart part);
    HotbarAvailability getHotbarAvailability();
    boolean isSlotLocked(int playerSlot);
    boolean isSlotInvisible(int playerSlot);
    InventoryShape getInventoryShape();

    enum HotbarAvailability {
        FULL, HANDS, NONE
    }

}
