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
package ladysnake.requiem.common.block.obelisk;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import ladysnake.requiem.api.v1.block.ObeliskRune;
import ladysnake.requiem.common.tag.RequiemBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ObeliskMatcher {
    public static final Direction[] OBELISK_SIDES = {Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
    public static final int MAX_OBELISK_CORE_WIDTH = 5;
    public static final int MAX_OBELISK_CORE_HEIGHT = 20;
    public static final DataResult<ObeliskMatch> INVALID_BASE = DataResult.error("Structure does not have a matching base");
    public static final DataResult<ObeliskMatch> INVALID_CORE = DataResult.error("Structure does not have a matching runic core");
    public static final DataResult<ObeliskMatch> INVALID_CAP = DataResult.error("Structure does not have a matching cap");
    public static final int NO_MATCH = 0;

    public static DataResult<ObeliskMatch> matchObelisk(World world, BlockPos origin) {
        int tentativeWidth = getObeliskCoreWidth(world, origin);
        if (tentativeWidth != NO_MATCH && matchObeliskBase(world, origin, tentativeWidth)) {
            ObeliskMatch attempt = matchObeliskCore(world, origin, tentativeWidth);
            if (attempt.coreHeight() > 0) {
                if (matchObeliskCap(world, origin, tentativeWidth, attempt.coreHeight())) {
                    return DataResult.success(attempt);
                }
                return INVALID_CAP;
            }
            return INVALID_CORE;
        }
        return INVALID_BASE;
    }

    private static int getObeliskCoreWidth(World world, BlockPos origin) {
        // We assume that the origin got already tested
        int sideLength = 1;

        while (world.getBlockState(origin.offset(OBELISK_SIDES[0], sideLength)).isIn(RequiemBlockTags.OBELISK_CORE)) {
            sideLength++;

            if (sideLength > MAX_OBELISK_CORE_WIDTH) {
                // Too large: not a valid obelisk
                return NO_MATCH;
            }
        }

        return sideLength;
    }

    private static ObeliskMatch matchObeliskCore(World world, BlockPos origin, int coreWidth) {
        // start at north-west corner
        BlockPos start = origin.add(-1, 0, -1);
        BlockPos.Mutable pos = start.mutableCopy();
        List<RuneSearchResult> layers = new ArrayList<>();
        Set<Text> names = new HashSet<>();
        int height;

        for (height = 0; height < MAX_OBELISK_CORE_HEIGHT; height++) {
            if (!testCoreLayerFrame(world, start, pos, coreWidth, height)) break;
            RuneSearchResult result = findRune(world, origin, coreWidth, height);
            if (!result.valid()) break;
            layers.add(result);
            names.addAll(result.names());
        }

        return new ObeliskMatch(origin, coreWidth, height, layers, names);
    }

    private static boolean testCoreLayerFrame(World world, BlockPos start, BlockPos.Mutable pos, int width, int height) {
        return world.getBlockState(pos.set(start.getX(), start.getY() + height, start.getZ())).isIn(RequiemBlockTags.OBELISK_FRAME)
            && world.getBlockState(pos.set(start.getX() + width + 1, start.getY() + height, start.getZ())).isIn(RequiemBlockTags.OBELISK_FRAME)
            && world.getBlockState(pos.set(start.getX() + width + 1, start.getY() + height, start.getZ() + width + 1)).isIn(RequiemBlockTags.OBELISK_FRAME)
            && world.getBlockState(pos.set(start.getX(), start.getY() + height, start.getZ() + width + 1)).isIn(RequiemBlockTags.OBELISK_FRAME);
    }

    private static RuneSearchResult findRune(World world, BlockPos origin, int width, int height) {
        Set<Text> names = new HashSet<>();
        ObeliskRune rune = null;
        boolean first = true;

        for (BlockPos corePos : iterateCoreBlocks(origin, width, height)) {
            BlockState state = world.getBlockState(corePos);
            if (state.isIn(RequiemBlockTags.OBELISK_CORE)) {
                BlockEntity be = state.hasBlockEntity() ? world.getBlockEntity(corePos) : null;
                @Nullable ObeliskRune foundRune = ObeliskRune.LOOKUP.find(world, corePos, state, be, null);
                if (first) {
                    rune = foundRune;
                    first = false;
                    if (be instanceof RunestoneBlockEntity runestone) runestone.getCustomName().ifPresent(names::add);
                } else if (foundRune != rune) {
                    return RuneSearchResult.FAILED;
                }
            } else {
                return RuneSearchResult.FAILED;
            }
        }

        return RuneSearchResult.of(rune, names);
    }

    private static boolean matchObeliskBase(BlockView world, BlockPos origin, int coreWidth) {
        return checkObeliskExtremity(world, origin.add(-1, -1, -1), coreWidth + 2);
    }

    private static boolean matchObeliskCap(BlockView world, BlockPos origin, int coreWidth, int height) {
        return checkObeliskExtremity(world, origin.add(-1, height, -1), coreWidth + 2);
    }

    static Iterable<BlockPos> iterateCoreBlocks(BlockPos origin, int coreWidth, int height) {
        return BlockPos.iterate(origin.up(height), origin.add(coreWidth - 1, height, coreWidth - 1));
    }

    private static boolean checkObeliskExtremity(BlockView world, BlockPos origin, int expectedWidth) {
        // We start at bottom north-west corner
        BlockPos.Mutable pos = origin.mutableCopy();

        for (Direction direction : OBELISK_SIDES) {
            int sideLength = 0;

            while (true) {
                if (!world.getBlockState(pos).isIn(RequiemBlockTags.OBELISK_FRAME)) {
                    return false;
                }

                sideLength++;

                if (sideLength < expectedWidth) {
                    pos.move(direction);
                } else {
                    break;
                }
            }
        }

        return true;
    }

    public static Optional<BlockPos> findObeliskOrigin(World world, BlockPos pos) {
        while (true) {
            ObeliskOriginMatch match = tryMatchObeliskOrigin(world, pos);
            if (match.origin != null) return Optional.of(match.origin);
            else if (match.delegate == null) return Optional.empty();
            else pos = match.delegate;
        }
    }

    /**
     * Finds the bottommost northwest corner of an obelisk.
     *
     * <p>If a {@link RunestoneBlockEntity} is found along the way, the search will be aborted
     * and a potential delegate will be set.
     * This method does not check that there is a valid obelisk, however it will abort and return
     * {@code null} if the core below this block entity does not conform
     * to expectations.
     *
     * @param world the world in which to search for the origin of an obelisk
     * @param pos   the position from which to initiate the search
     * @return an {@link Either} describing either a potential origin for an obelisk, or the position of a better-suited delegate
     */
    private static ObeliskOriginMatch tryMatchObeliskOrigin(World world, BlockPos pos) {
        if (!(world.getBlockState(pos.west()).isIn(RequiemBlockTags.OBELISK_CORE))) {
            if (!(world.getBlockState(pos.north()).isIn(RequiemBlockTags.OBELISK_CORE))) {
                // we are at the northwest corner, now we go down until we find either the floor or a better candidate
                // this should not go on for too many blocks, so mutable blockpos should not be necessary
                BlockPos obeliskOrigin = pos;
                BlockPos down = obeliskOrigin.down();
                BlockState downState = world.getBlockState(down);

                while (downState.isIn(RequiemBlockTags.OBELISK_CORE)) {
                    obeliskOrigin = down;
                    down = obeliskOrigin.down();

                    if (world.getBlockEntity(down) instanceof RunestoneBlockEntity) {
                        // Found a better candidate in a lower core layer
                        return ObeliskOriginMatch.partial(down);
                    }

                    downState = world.getBlockState(down);
                }

                return ObeliskOriginMatch.success(obeliskOrigin);
            } else {
                // Found a better candidate in the same layer
                return ObeliskOriginMatch.partial(pos.north());
            }
        } else {
            // Found a better candidate in the same layer
            return ObeliskOriginMatch.partial(pos.west());
        }
    }

    record RuneSearchResult(boolean valid, @Nullable ObeliskRune rune, Set<Text> names) {
        static final RuneSearchResult FAILED = new RuneSearchResult(false, null, Set.of());
        static final RuneSearchResult INERT = new RuneSearchResult(true, null, Set.of());

        static RuneSearchResult of(@Nullable ObeliskRune rune, Set<Text> names) {
            return rune == null && names.isEmpty() ? INERT : new RuneSearchResult(true, rune, names);
        }
    }

    private static final class ObeliskOriginMatch {
        private final @Nullable BlockPos origin;
        private final @Nullable BlockPos delegate;

        public static ObeliskOriginMatch success(BlockPos origin) {
            return new ObeliskOriginMatch(origin, null);
        }

        public static ObeliskOriginMatch partial(BlockPos delegate) {
            return new ObeliskOriginMatch(null, delegate);
        }

        private ObeliskOriginMatch(@Nullable BlockPos origin, @Nullable BlockPos delegate) {
            this.origin = origin;
            this.delegate = delegate;
        }
    }
}
