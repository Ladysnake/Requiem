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
package ladysnake.requiem.api.v1.remnant;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import ladysnake.requiem.api.v1.block.ObeliskDescriptor;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import net.minecraft.util.Identifier;

import java.util.Set;

public interface RiftTracker extends Component {
    ComponentKey<RiftTracker> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "rift_tracker"), RiftTracker.class);

    /**
     * @throws IllegalArgumentException if {@code riftRecord} does not describe an obelisk with a rift runestone
     */
    void addRift(GlobalRecord riftRecord);
    Set<ObeliskDescriptor> fetchKnownObelisks();
}
