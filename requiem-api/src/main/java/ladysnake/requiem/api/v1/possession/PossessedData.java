/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.api.v1.possession;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public interface PossessedData extends ClientTickingComponent {
    ComponentKey<PossessedData> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "possessed_data"), PossessedData.class);

    NbtCompound getHungerData();

    void setConvertedUnderPossession();

    boolean wasConvertedUnderPossession();

    void moveItems(PlayerInventory inventory, boolean fromPlayerToThis);

    void giftFirstPossessionLoot(PlayerEntity player);

    void dropItems();

    void copyFrom(PossessedData original);
}
