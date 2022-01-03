/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.block;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ladysnake.requiem.api.v1.block.ObeliskRune;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ObeliskMatch {
    private final BlockPos origin;
    private final int coreWidth;
    private final int coreHeight;
    private final List<RunestoneBlockEntity.RuneSearchResult> layers;
    private final Set<Text> names;

    ObeliskMatch(BlockPos origin, int coreWidth, int coreHeight, List<RunestoneBlockEntity.RuneSearchResult> coreLayers, Set<Text> names) {
        this.names = names;
        Preconditions.checkArgument(coreHeight == coreLayers.size());
        this.origin = origin;
        this.coreWidth = coreWidth;
        this.coreHeight = coreHeight;
        this.layers = coreLayers;
    }

    public Stream<BlockPos> runePositions() {
        return IntStream.range(0, this.coreHeight)
            .filter(height -> layers.get(height).rune() != null)
            .mapToObj(height -> RunestoneBlockEntity.iterateCoreBlocks(this.origin, this.coreWidth, height))
            .flatMap(positions -> StreamSupport.stream(positions.spliterator(), false));
    }

    public Set<Text> names() {
        return names;
    }

    public BlockPos origin() {
        return this.origin;
    }

    public int coreWidth() {
        return this.coreWidth;
    }

    public int coreHeight() {
        return this.coreHeight;
    }

    public Object2IntMap<ObeliskRune> collectRunes() {
        Object2IntMap<ObeliskRune> levels = new Object2IntOpenHashMap<>();
        for (RunestoneBlockEntity.RuneSearchResult result : this.layers) {
            if (result.rune() != null && levels.getInt(result.rune()) < result.rune().getMaxLevel()) {
                levels.mergeInt(result.rune(), 1, Integer::sum);
            }
        }
        return levels;
    }
}
