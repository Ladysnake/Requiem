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
package ladysnake.requiem.api.v1.block;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;

/**
 * Interface to be provided by blocks to display a special icon when targeted by a {@linkplain RemnantComponent#isVagrant() vagrant} player.
 *
 * <p>Blocks that provide this interface still need to be added to the {@code requiem:soul_interactable} block tag if they want to allow right-clicking.
 */
@API(status = API.Status.EXPERIMENTAL, since = "2.0.0")
public interface VagrantTargetableBlock {
    BlockApiLookup<VagrantTargetableBlock, Void> LOOKUP = BlockApiLookup.get(new Identifier("requiem", "remnant_focusable"), VagrantTargetableBlock.class, Void.class);

    Identifier getTargetedIcon();

    boolean canBeUsedByVagrant(PlayerEntity player);
}
