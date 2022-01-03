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

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;

/**
 * Interface to be provided by blocks to apply effects when used in an obelisk.
 *
 * <p>Blocks that provide this interface still need to be added to the {@code requiem:obelisk/core} block tag.
 *
 * <p>Example registration code:
 * <pre>{@code
 *      ObeliskRune myRune = new MyObeliskRuneImpl();
 *      ObeliskRune.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) -> myRune, myBlock);
 * }</pre>
 */
@API(status = API.Status.EXPERIMENTAL)
public interface ObeliskRune {
    BlockApiLookup<ObeliskRune, Void> LOOKUP = BlockApiLookup.get(new Identifier("requiem", "obelisk_rune"), ObeliskRune.class, Void.class);

    /**
     * @return the maximum amount of levels of runes of this type that can be active on a single obelisk
     */
    int getMaxLevel();

    void applyEffect(ServerPlayerEntity target, int runeLevel, int obeliskWidth);
}
